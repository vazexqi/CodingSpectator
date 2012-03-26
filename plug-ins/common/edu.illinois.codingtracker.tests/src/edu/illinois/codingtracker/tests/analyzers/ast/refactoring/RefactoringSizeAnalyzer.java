/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers.ast.refactoring;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
			currentAutomatedRefactoringSize= 0;
			currentAutomatedRefactoringTimestamp= userOperation.getTime();
		}
		if (userOperation instanceof FinishedRefactoringOperation) {
			handleFinishedRefactoring((FinishedRefactoringOperation)userOperation);
		}
	}

	private void handleASTOperation(ASTOperation operation) {
		long refactoringID= operation.getRefactoringID();
		if (refactoringID != -1) {
			int currentSize= getAccumulatedRefactoringSize(refactoringID);
			inferredRefactoringSizes.put(refactoringID, currentSize + 1);
		}
		if (isInsideAutomatedRefactoring()) {
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
			appendCSVEntry(postprocessedUsername, postprocessedWorkspaceID, postprocessedVersion,
					refactoringDescriptor.timestamp, refactoringDescriptor.isAutomated ? "AUTOMATED" : "MANUAL",
							refactoringDescriptor.refactoringKind, refactoringDescriptor.size);
		}
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

}
