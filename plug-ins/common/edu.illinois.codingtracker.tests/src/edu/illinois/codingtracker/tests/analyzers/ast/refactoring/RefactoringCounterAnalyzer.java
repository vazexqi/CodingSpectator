/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers.ast.refactoring;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.ast.InferredRefactoringOperation;
import edu.illinois.codingtracker.operations.ast.InferredRefactoringOperation.RefactoringKind;
import edu.illinois.codingtracker.operations.refactorings.FinishedRefactoringOperation;


/**
 * This analyzer calculates for each kind of refactorings that we infer, how many manual and
 * automated refactorings were performed.
 * 
 * @author Stas Negara
 * 
 */
public class RefactoringCounterAnalyzer extends InferredRefactoringAnalyzer {

	private final Map<RefactoringKind, RefactoringCounter> refactorings= new HashMap<RefactoringKind, RefactoringCounter>();


	@Override
	protected String getTableHeader() {
		return "username,workspace ID,version,refactoring kind,manual count,automated count\n";
	}

	@Override
	protected void postprocessOperation(UserOperation userOperation) {
		if (userOperation instanceof InferredRefactoringOperation) {
			handleInferredRefactoring((InferredRefactoringOperation)userOperation);
		} else if (userOperation instanceof FinishedRefactoringOperation) {
			handleFinishedRefactoring((FinishedRefactoringOperation)userOperation);
		}
	}

	private void handleInferredRefactoring(InferredRefactoringOperation inferredRefactoring) {
		if (!shouldIgnoreInferredRefactoring(inferredRefactoring)) {
			incrementManualCounter(inferredRefactoring.getRefactoringKind());
		}
	}

	private void handleFinishedRefactoring(FinishedRefactoringOperation finishedRefactoring) {
		if (!shouldIgnoreAutomatedRefactoring(finishedRefactoring)) {
			incrementAutomatedCounter(getCurrentAutomatedRefactoringKind());
		}
	}

	private void incrementAutomatedCounter(RefactoringKind refactoringKind) {
		getRefactoringCounter(refactoringKind).automatedRefactoringCount++;
	}

	private void incrementManualCounter(RefactoringKind refactoringKind) {
		getRefactoringCounter(refactoringKind).manualRefactoringCount++;
	}

	private RefactoringCounter getRefactoringCounter(RefactoringKind refactoringKind) {
		RefactoringCounter refactoringCounter= refactorings.get(refactoringKind);
		if (refactoringCounter == null) {
			refactoringCounter= new RefactoringCounter();
			refactorings.put(refactoringKind, refactoringCounter);
		}
		return refactoringCounter;
	}

	@Override
	protected void initialize() {
		super.initialize();
		refactorings.clear();
	}

	@Override
	protected void populateResults() {
		for (Entry<RefactoringKind, RefactoringCounter> entry : refactorings.entrySet()) {
			appendCSVEntry(postprocessedUsername, postprocessedWorkspaceID, postprocessedVersion, entry.getKey().name(),
					entry.getValue().manualRefactoringCount, entry.getValue().automatedRefactoringCount);
		}
	}

	@Override
	protected String getResultFilePostfix() {
		return ".refactoring_counts";
	}

	private class RefactoringCounter {

		private int manualRefactoringCount= 0;

		private int automatedRefactoringCount= 0;

	}

}
