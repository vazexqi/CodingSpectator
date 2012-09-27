/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers.ast.refactoring;

import java.util.LinkedList;
import java.util.List;

import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.operations.ast.InferredRefactoringOperation;
import edu.illinois.codingtracker.operations.refactorings.FinishedRefactoringOperation;
import edu.illinois.codingtracker.operations.refactorings.NewStartedRefactoringOperation;


/**
 * This analyzer detects root canal refactorings, calculates their fraction in the total number of
 * refactorings, and the average gap (expressed in AST node operations) between floss refactorings.
 * 
 * @author Stas Negara
 * 
 */
public class RootCanalAndFlossRefactoringAnalyzer extends InferredRefactoringAnalyzer {

	private final int rootCanalRefactoringMinSize= 3;

	private final int rootCanalMaxThreshold= 10;

	@SuppressWarnings("unchecked")
	private final List<RefactoringDescriptor>[] currentCandidates= new LinkedList[rootCanalMaxThreshold];

	private int[] currentCompleteCandidatesCount;

	private int[] currentRootCanalSize;

	private int[] rootCanalRefactoringsCount;

	private int[] editsInBetweenFlossRefactoringsCount;

	private int refactoringsCount;

	private int[] totalRootCanalRefactoringsCount= new int[rootCanalMaxThreshold];

	private int[] totalEditsInBetweenFlossRefactoringsCount= new int[rootCanalMaxThreshold];

	private int totalRefactoringsCount= 0;


	@Override
	protected String getTableHeader() {
		return "username,workspace ID,version,total refactorings count,root canal 1,root canal 2,root canal 3,root canal 4,root canal 5,root canal 6,root canal 7,root canal 8,root canal 9,root canal 10,floss gaps 1,floss gaps 2,floss gaps 3,floss gaps 4,floss gaps 5,floss gaps 6,floss gaps 7,floss gaps 8,floss gaps 9,floss gaps 10\n";
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
			addNewRefactoring(-1); //Automated refactorings are assigned ID -1.
		}
		if (userOperation instanceof FinishedRefactoringOperation) {
			handleCompletedRefactoring();
		}
	}

	private void handleInferredRefactoring(InferredRefactoringOperation inferredRefactoring) {
		if (isInsideAutomatedRefactoring()) {
			//If the inferred refactoring started before the automated one, it should be counted separately.
			if (isCurrentCandidateID(inferredRefactoring.getRefactoringID())) {
				incrementCurrentCompleteCandidatesCount();
			}
		} else {
			handleCompletedRefactoring();
		}
	}

	private void handleCompletedRefactoring() {
		incrementCurrentCompleteCandidatesCount();
		for (int i= 0; i < rootCanalMaxThreshold; i++) {
			handleCompletedRefactoring(i);
		}
	}

	private void handleCompletedRefactoring(int index) {
		//Look for all candidate refactorings to be completed - this accounts for overlaps between refactorings. 
		if (currentCompleteCandidatesCount[index] == currentCandidates[index].size()) {
			while (currentCompleteCandidatesCount[index] >= rootCanalRefactoringMinSize) {
				if (isRootCanal(index)) {
					//Expand the size of the root canal refactoring.
					currentRootCanalSize[index]= currentCompleteCandidatesCount[index];
					return;
				} else if (currentRootCanalSize[index] != 0) {
					reportFoundRootCanalRefactoring(index);
				} else {
					dropFlossRefactoring(index);
				}
			}
		}
	}

	private void dropFlossRefactoring(int index) {
		RefactoringDescriptor removedCandidate= currentCandidates[index].remove(0);
		editsInBetweenFlossRefactoringsCount[index]+= removedCandidate.editedASTNodesSinceStart - currentCandidates[index].get(0).editedASTNodesSinceStart;
		currentCompleteCandidatesCount[index]--;
	}

	private void incrementCurrentCompleteCandidatesCount() {
		for (int i= 0; i < rootCanalMaxThreshold; i++) {
			currentCompleteCandidatesCount[i]++;
		}
	}

	private void reportFoundRootCanalRefactoring(int index) {
		//System.out.println("Found root canal, index= " + index + ", size= " + currentRootCanalSize[index]);
		rootCanalRefactoringsCount[index]+= currentRootCanalSize[index];
		//Remove the first currentRootCanalSize count of candidates.
		for (int i= 0; i < currentRootCanalSize[index]; i++) {
			currentCandidates[index].remove(0);
		}
		currentCompleteCandidatesCount[index]-= currentRootCanalSize[index];
		currentRootCanalSize[index]= 0;
	}

	private boolean isRootCanal(int index) {
		//The first refactoring has all refactored and edited AST nodes for the whole batch of candidates. 
		RefactoringDescriptor firstCandidate= currentCandidates[index].get(0);
		//Root canal threshold is (index + 1).
		if (firstCandidate.editedASTNodesSinceStart == 0 ||
				firstCandidate.refactoredASTNodesSinceStart / firstCandidate.editedASTNodesSinceStart >= index + 1) {
			return true;
		}
		return false;
	}

	private void handleASTOperation(ASTOperation operation) {
		if (isInsideAutomatedRefactoring()) {
			if (!isInsideNoisyRefactoring()) {
				incrementCurrentRefactoredNodes();
			}
		} else {
			long refactoringID= operation.getTransformationID();
			if (refactoringID != -1) {
				if (!isCurrentCandidateID(refactoringID)) {
					addNewRefactoring(refactoringID);
				}
				incrementCurrentRefactoredNodes();
			} else {
				incrementCurrentEditedNodes();
			}
		}
	}

	private void addNewRefactoring(long refactoringID) {
		refactoringsCount++;
		for (int i= 0; i < rootCanalMaxThreshold; i++) {
			currentCandidates[i].add(new RefactoringDescriptor(refactoringID));
		}
	}

	private boolean isInsideNoisyRefactoring() {
		String refactoringID= getCurrentAutomatedRefactoringID();
		return refactoringID.contains("delete") || refactoringID.contains("copy");
	}

	private void incrementCurrentEditedNodes() {
		for (int i= 0; i < rootCanalMaxThreshold; i++) {
			for (RefactoringDescriptor currentCandidate : currentCandidates[i]) {
				currentCandidate.editedASTNodesSinceStart++;
			}
		}
	}

	private void incrementCurrentRefactoredNodes() {
		for (int i= 0; i < rootCanalMaxThreshold; i++) {
			for (RefactoringDescriptor currentCandidate : currentCandidates[i]) {
				currentCandidate.refactoredASTNodesSinceStart++;
			}
		}
	}

	private boolean isCurrentCandidateID(long refactoringID) {
		//It is sufficient to check just for one index (e.g., 0).
		for (RefactoringDescriptor currentCandidate : currentCandidates[0]) {
			if (currentCandidate.refactoringID == refactoringID) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void initialize() {
		super.initialize();
		for (int i= 0; i < rootCanalMaxThreshold; i++) {
			currentCandidates[i]= new LinkedList<RefactoringDescriptor>();
		}
		currentCompleteCandidatesCount= new int[rootCanalMaxThreshold];
		currentRootCanalSize= new int[rootCanalMaxThreshold];
		rootCanalRefactoringsCount= new int[rootCanalMaxThreshold];
		editsInBetweenFlossRefactoringsCount= new int[rootCanalMaxThreshold];
		refactoringsCount= 0;
	}

	private void flushRefactoringsData() {
		for (int i= 0; i < rootCanalMaxThreshold; i++) {
			if (currentRootCanalSize[i] != 0) {
				reportFoundRootCanalRefactoring(i);
			}
			while (currentCandidates[i].size() > 1) {
				dropFlossRefactoring(i);
			}
		}
	}

	@Override
	protected void populateResults() {
		flushRefactoringsData();
		updateTotalCounts();
		appendCSVEntry(postprocessedUsername, postprocessedWorkspaceID, postprocessedVersion, refactoringsCount,
						rootCanalRefactoringsCount[0], rootCanalRefactoringsCount[1],
						rootCanalRefactoringsCount[2], rootCanalRefactoringsCount[3],
						rootCanalRefactoringsCount[4], rootCanalRefactoringsCount[5],
						rootCanalRefactoringsCount[6], rootCanalRefactoringsCount[7],
						rootCanalRefactoringsCount[8], rootCanalRefactoringsCount[9],
						getEditsPerFlossRefactoring(0), getEditsPerFlossRefactoring(1),
						getEditsPerFlossRefactoring(2), getEditsPerFlossRefactoring(3),
						getEditsPerFlossRefactoring(4), getEditsPerFlossRefactoring(5),
						getEditsPerFlossRefactoring(6), getEditsPerFlossRefactoring(7),
						getEditsPerFlossRefactoring(8), getEditsPerFlossRefactoring(9));
	}

	private int getEditsPerFlossRefactoring(int index) {
		int flossRefactoringsCount= refactoringsCount - rootCanalRefactoringsCount[index];
		if (flossRefactoringsCount == 0) {
			return 0;
		}
		return editsInBetweenFlossRefactoringsCount[index] / flossRefactoringsCount;
	}

	private void updateTotalCounts() {
		totalRefactoringsCount+= refactoringsCount;
		for (int i= 0; i < rootCanalMaxThreshold; i++) {
			totalRootCanalRefactoringsCount[i]+= rootCanalRefactoringsCount[i];
			totalEditsInBetweenFlossRefactoringsCount[i]+= editsInBetweenFlossRefactoringsCount[i];
		}
	}

	@Override
	protected void finishedProcessingAllSequences() {
		System.out.println("Total root canal and floss refactoring statistics:");
		System.out.print(totalRefactoringsCount + ",");
		for (int i= 0; i < rootCanalMaxThreshold; i++) {
			System.out.print(totalRootCanalRefactoringsCount[i] + ",");
		}
		for (int i= 0; i < rootCanalMaxThreshold; i++) {
			System.out.print(getTotalEditsPerFlossRefactoring(i) + ",");
		}
	}

	private int getTotalEditsPerFlossRefactoring(int index) {
		int totalFlossRefactoringsCount= totalRefactoringsCount - totalRootCanalRefactoringsCount[index];
		if (totalFlossRefactoringsCount == 0) {
			return 0;
		}
		return totalEditsInBetweenFlossRefactoringsCount[index] / totalFlossRefactoringsCount;
	}

	@Override
	protected String getResultFilePostfix() {
		return ".root_canal";
	}

	private class RefactoringDescriptor {
		final long refactoringID;

		int refactoredASTNodesSinceStart= 0;

		int editedASTNodesSinceStart= 0;

		RefactoringDescriptor(long refactoringID) {
			this.refactoringID= refactoringID;
		}

	}

}
