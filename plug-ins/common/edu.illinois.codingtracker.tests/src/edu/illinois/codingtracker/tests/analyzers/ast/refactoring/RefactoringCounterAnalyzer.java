/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers.ast.refactoring;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.ast.InferredRefactoringOperation;
import edu.illinois.codingtracker.operations.ast.InferredRefactoringOperation.RefactoringKind;
import edu.illinois.codingtracker.operations.refactorings.FinishedRefactoringOperation;
import edu.illinois.codingtracker.operations.refactorings.NewStartedRefactoringOperation;
import edu.illinois.codingtracker.operations.refactorings.NewStartedRefactoringOperation.RefactoringMode;
import edu.illinois.codingtracker.tests.analyzers.CSVProducingAnalyzer;


/**
 * This analyzer calculates for each kind of refactorings that we infer, how many manual and
 * automated refactorings were performed.
 * 
 * @author Stas Negara
 * 
 */
public class RefactoringCounterAnalyzer extends CSVProducingAnalyzer {

	private static final long renameAfterExtractThreshold= 10 * 1000; //10 seconds.

	private final Map<RefactoringKind, RefactoringCounter> refactorings= new HashMap<RefactoringKind, RefactoringCounter>();

	private boolean isInsideAutomatedRefactoring;

	private RefactoringKind currentAutomatedRefactoringKind;

	private String extractedEntityName;

	private long finishedAutomatedRefactoringTimestamp;


	@Override
	protected String getTableHeader() {
		return "username,workspace ID,refactoring kind,manual count,automated count\n";
	}

	@Override
	protected void checkPostprocessingPreconditions() {
		//no preconditions
	}

	@Override
	protected boolean shouldPostprocessVersionFolder(String folderName) {
		return true;
	}

	@Override
	protected String getRecordFileName() {
		return "codechanges.txt.inferred_ast_operations.with_move.with_inferred_refactorings";
	}

	@Override
	protected void postprocess(List<UserOperation> userOperations) {
		initialize();
		for (UserOperation userOperation : userOperations) {
			if (userOperation instanceof NewStartedRefactoringOperation) {
				handleStartedRefactoring((NewStartedRefactoringOperation)userOperation);
			} else if (userOperation instanceof FinishedRefactoringOperation) {
				handleFinishedRefactoring((FinishedRefactoringOperation)userOperation);
			} else if (userOperation instanceof InferredRefactoringOperation) {
				handleInferredRefactoringOutsideAutomatedRefactoring((InferredRefactoringOperation)userOperation);
			}
		}
		populateResults();
	}

	private void handleInferredRefactoringOutsideAutomatedRefactoring(InferredRefactoringOperation inferredRefactoring) {
		if (!shouldIgnoreInferredRefactoring(inferredRefactoring)) {
			incrementManualCounter(inferredRefactoring.getRefactoringKind());
		}
	}

	private boolean shouldIgnoreInferredRefactoring(InferredRefactoringOperation inferredRefactoring) {
		//TODO: Note that undoing a rename that follows an extract is still counted as a manual refactoring.
		return isInsideAutomatedRefactoring || InferredRefactoringOperation.isRename(inferredRefactoring.getRefactoringKind()) &&
				Math.abs(inferredRefactoring.getTime() - finishedAutomatedRefactoringTimestamp) < renameAfterExtractThreshold &&
				inferredRefactoring.getArguments().get("OldName").equals(extractedEntityName);
	}

	private void handleFinishedRefactoring(FinishedRefactoringOperation finishedRefactoringOperation) {
		if (currentAutomatedRefactoringKind != null && !finishedRefactoringOperation.isTooSimple()) {
			incrementAutomatedCounter(currentAutomatedRefactoringKind);
		}
		finishedAutomatedRefactoringTimestamp= finishedRefactoringOperation.getTime();
		resetRefactoringState();
	}

	private void handleStartedRefactoring(NewStartedRefactoringOperation startedRefactoringOperation) {
		isInsideAutomatedRefactoring= true;
		//Consider only performed refactorins.
		if (startedRefactoringOperation.getRefactoringMode() == RefactoringMode.PERFORM) {
			currentAutomatedRefactoringKind= getRefactoringKind(startedRefactoringOperation);
			if (InferredRefactoringOperation.isExtract(currentAutomatedRefactoringKind)) {
				extractedEntityName= startedRefactoringOperation.getArguments().get("name");
			} else {
				extractedEntityName= null;
			}
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

	private void initialize() {
		result= new StringBuffer();
		refactorings.clear();
		extractedEntityName= null;
		finishedAutomatedRefactoringTimestamp= -1;
		resetRefactoringState();
	}

	private void resetRefactoringState() {
		isInsideAutomatedRefactoring= false;
		currentAutomatedRefactoringKind= null;
	}

	private void populateResults() {
		for (Entry<RefactoringKind, RefactoringCounter> entry : refactorings.entrySet()) {
			appendCSVEntry(postprocessedUsername, postprocessedWorkspaceID, entry.getKey().name(), entry.getValue().manualRefactoringCount, entry.getValue().automatedRefactoringCount);
		}
	}

	@Override
	protected String getResultFilePostfix() {
		return ".refactoring_counts";
	}

	@Override
	protected boolean shouldMergeResults() {
		return true;
	}

	@Override
	protected boolean shouldOutputIndividualResults() {
		return false;
	}

	private static RefactoringKind getRefactoringKind(NewStartedRefactoringOperation startedRefactoringOperation) {
		String refactoringName= startedRefactoringOperation.getID().substring("org.eclipse.jdt.ui.".length());
		if (refactoringName.equals("extract.temp")) {
			return RefactoringKind.EXTRACT_LOCAL_VARIABLE;
		}
		if (refactoringName.equals("inline.temp")) {
			return RefactoringKind.INLINE_LOCAL_VARIABLE;
		}
		if (refactoringName.equals("rename.local.variable")) {
			return RefactoringKind.RENAME_LOCAL_VARIABLE;
		}
		if (refactoringName.equals("rename.field")) {
			return RefactoringKind.RENAME_FIELD;
		}
		if (refactoringName.equals("rename.method")) {
			return RefactoringKind.RENAME_METHOD;
		}
		if (refactoringName.equals("rename.type")) {
			return RefactoringKind.RENAME_CLASS;
		}
		if (refactoringName.equals("promote.temp")) {
			return RefactoringKind.PROMOTE_TEMP;
		}
		if (refactoringName.equals("extract.constant")) {
			return RefactoringKind.EXTRACT_CONSTANT;
		}
		if (refactoringName.equals("extract.method")) {
			return RefactoringKind.EXTRACT_METHOD;
		}
		if (refactoringName.equals("self.encapsulate")) {
			return RefactoringKind.ENCAPSULATE_FIELD;
		}
		return null;
	}

	private class RefactoringCounter {

		private int manualRefactoringCount= 0;

		private int automatedRefactoringCount= 0;

	}

}
