/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers.ast.refactoring;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.operations.ast.InferredRefactoringOperation;
import edu.illinois.codingtracker.operations.ast.InferredRefactoringOperation.RefactoringKind;
import edu.illinois.codingtracker.operations.refactorings.FinishedRefactoringOperation;
import edu.illinois.codingtracker.operations.refactorings.NewStartedRefactoringOperation;


/**
 * This analyzer calculates for each manual or automated refactoring its size expressed in the
 * number of the affected AST nodes.
 * 
 * @author Stas Negara
 * 
 */
public class RefactoringSizeAnalyzer extends InferredRefactoringAnalyzer {

	private final List<RefactoringDescriptor> refactoringDescriptors= new LinkedList<RefactoringDescriptor>();

	private final Map<Long, Integer> inferredRefactoringSizes= new HashMap<Long, Integer>();

	private int currentAutomatedRefactoringSize;

	private long currentAutomatedRefactoringTimestamp;

	private Set<Long> currentAutomatedRefactoringAffectedNodeIDs= new HashSet<Long>();

	private final Map<RefactoringKind, TotalSize> totalRefactoringSizes= new HashMap<RefactoringKind, TotalSize>();

	private int totalASTOperationsCount= 0;

	private int totalRefactoringASTOperationsCount= 0;


	@Override
	protected String getTableHeader() {
		return "username,workspace ID,version,timestamp,how performed,refactoring kind,size\n";
	}

	@Override
	protected void postprocessOperation(UserOperation userOperation) {
		if (userOperation instanceof ASTOperation) {
			handleASTOperation((ASTOperation)userOperation);
		}
		if (userOperation instanceof InferredRefactoringOperation) {
			handleInferredRefactoring((InferredRefactoringOperation)userOperation);
		}
		if (userOperation instanceof NewStartedRefactoringOperation) {
			handleStartedRefactoring((NewStartedRefactoringOperation)userOperation);
		}
		if (userOperation instanceof FinishedRefactoringOperation) {
			handleFinishedRefactoring((FinishedRefactoringOperation)userOperation);
		}
	}

	private void handleStartedRefactoring(NewStartedRefactoringOperation startedRefactoring) {
		currentAutomatedRefactoringSize= 0;
		currentAutomatedRefactoringTimestamp= startedRefactoring.getTime();
		currentAutomatedRefactoringAffectedNodeIDs.clear();
	}

	private void handleASTOperation(ASTOperation operation) {
		totalASTOperationsCount++;
		long refactoringID= operation.getTransformationID();
		if (refactoringID != -1) {
			int currentSize= getAccumulatedRefactoringSize(refactoringID);
			inferredRefactoringSizes.put(refactoringID, currentSize + 1);
		}
		if (refactoringID != -1 || isInsideAutomatedRefactoring()) {
			totalRefactoringASTOperationsCount++;
		}
		if (isInsideAutomatedRefactoring()) {
			long nodeID= operation.getNodeID();
			if (operation.isDelete() && currentAutomatedRefactoringAffectedNodeIDs.contains(nodeID)) {
				//Instead of adding an additional operation, discount the previously counted operation.
				currentAutomatedRefactoringSize--;
				return;
			}
			currentAutomatedRefactoringAffectedNodeIDs.add(nodeID);
			currentAutomatedRefactoringSize++;
		}
	}

	private int getAccumulatedRefactoringSize(long refactoringID) {
		Integer size= inferredRefactoringSizes.get(refactoringID);
		if (size == null) {
			return 0;
		}
		return size;
	}

	private void handleInferredRefactoring(InferredRefactoringOperation inferredRefactoring) {
		if (!shouldIgnoreInferredRefactoring(inferredRefactoring)) {
			int size= getAccumulatedRefactoringSize(inferredRefactoring.getRefactoringID());
			refactoringDescriptors.add(new RefactoringDescriptor(inferredRefactoring.getTime(), false, inferredRefactoring.getRefactoringKind(), size));
		}
	}

	private void handleFinishedRefactoring(FinishedRefactoringOperation finishedRefactoring) {
		if (!shouldIgnoreAutomatedRefactoring(finishedRefactoring)) {
			refactoringDescriptors.add(new RefactoringDescriptor(currentAutomatedRefactoringTimestamp, true, getCurrentAutomatedRefactoringKind(), currentAutomatedRefactoringSize));
		}
	}

	@Override
	protected void initialize() {
		super.initialize();
		refactoringDescriptors.clear();
		inferredRefactoringSizes.clear();
	}

	@Override
	protected void populateResults() {
		for (RefactoringDescriptor refactoringDescriptor : refactoringDescriptors) {
			updateTotalRefactoringSizes(refactoringDescriptor);
			appendCSVEntry(postprocessedUsername, postprocessedWorkspaceID, postprocessedVersion,
					refactoringDescriptor.timestamp, refactoringDescriptor.isAutomated ? "AUTOMATED" : "MANUAL",
							refactoringDescriptor.refactoringKind, refactoringDescriptor.size);
		}
	}

	private void updateTotalRefactoringSizes(RefactoringDescriptor refactoringDescriptor) {
		TotalSize totalSize= totalRefactoringSizes.get(refactoringDescriptor.refactoringKind);
		if (totalSize == null) {
			totalSize= new TotalSize();
			totalRefactoringSizes.put(refactoringDescriptor.refactoringKind, totalSize);
		}
		if (refactoringDescriptor.isAutomated) {
			totalSize.addAutomatedSize(refactoringDescriptor.size);
		} else {
			totalSize.addManualSize(refactoringDescriptor.size);
		}
	}

	@Override
	protected void finishedProcessingAllSequences() {
		System.out.println("Total average sizes:");
		for (Entry<RefactoringKind, TotalSize> entry : totalRefactoringSizes.entrySet()) {
			TotalSize totalSize= entry.getValue();
			System.out.println(entry.getKey() + "," + totalSize.getManualCount() + "," + totalSize.getManualMean() + ","
					+ totalSize.getManualStDev() + "," + totalSize.getAutomatedCount() + "," + totalSize.getAutomatedMean() + ","
					+ totalSize.getAutomatedStDev());
		}
		System.out.println("Total AST operations count: " + totalASTOperationsCount);
		System.out.println("Total refactoring AST operations count: " + totalRefactoringASTOperationsCount);
	}

	@Override
	protected String getResultFilePostfix() {
		return ".refactoring_size";
	}

	private class RefactoringDescriptor {

		private final long timestamp;

		private final boolean isAutomated;

		private final RefactoringKind refactoringKind;

		private final int size;

		RefactoringDescriptor(long timestamp, boolean isAutomated, RefactoringKind refactoringKind, int size) {
			this.timestamp= timestamp;
			this.isAutomated= isAutomated;
			this.refactoringKind= refactoringKind;
			this.size= size;
		}

	}

	private class TotalSize {

		private List<Integer> automatedSizes= new LinkedList<Integer>();

		private List<Integer> manualSizes= new LinkedList<Integer>();


		void addAutomatedSize(int size) {
			automatedSizes.add(size);
		}

		void addManualSize(int size) {
			manualSizes.add(size);
		}

		int getAutomatedCount() {
			return automatedSizes.size();
		}

		int getManualCount() {
			return manualSizes.size();
		}

		double getAutomatedMean() {
			return getMean(automatedSizes);
		}

		double getAutomatedStDev() {
			return getStDev(automatedSizes);
		}

		double getManualStDev() {
			return getStDev(manualSizes);
		}

		double getManualMean() {
			return getMean(manualSizes);
		}

		private double getMean(List<Integer> sizes) {
			if (sizes.size() == 0) {
				return 0;
			}
			long totalSize= 0;
			for (int size : sizes) {
				totalSize+= size;
			}
			return (double)totalSize / sizes.size();
		}

		private double getStDev(List<Integer> sizes) {
			if (sizes.size() == 0) {
				return 0;
			}
			double mean= getMean(sizes);
			double squares= 0;
			for (int size : sizes) {
				squares+= (mean - size) * (mean - size);
			}
			return Math.sqrt(squares / sizes.size());
		}

	}

}
