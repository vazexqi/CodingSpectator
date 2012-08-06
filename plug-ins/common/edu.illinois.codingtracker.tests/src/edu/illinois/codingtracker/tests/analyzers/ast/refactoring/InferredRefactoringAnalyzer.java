/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers.ast.refactoring;

import java.util.List;

import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.ast.InferredRefactoringOperation;
import edu.illinois.codingtracker.operations.ast.InferredRefactoringOperation.RefactoringKind;
import edu.illinois.codingtracker.operations.refactorings.FinishedRefactoringOperation;
import edu.illinois.codingtracker.operations.refactorings.NewStartedRefactoringOperation;
import edu.illinois.codingtracker.operations.refactorings.NewStartedRefactoringOperation.RefactoringMode;
import edu.illinois.codingtracker.tests.analyzers.CSVProducingAnalyzer;


/**
 * This is a base class for all analyzers that involve the inferred refactorings. It detects manual
 * inferred refactorings as well as filters out ignored automated refactorings.
 * 
 * @author Stas Negara
 * 
 */
public abstract class InferredRefactoringAnalyzer extends CSVProducingAnalyzer {

	private static final long renameAfterExtractThreshold= 10 * 1000; //10 seconds.

	private boolean isInsideAutomatedRefactoring;

	private RefactoringKind currentAutomatedRefactoringKind;

	private String currentAutomatedRefactoringID;

	private String extractedEntityName;

	private long finishedAutomatedRefactoringTimestamp;


	protected abstract void postprocessOperation(UserOperation userOperation);

	protected abstract void populateResults();

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
			}
			postprocessOperation(userOperation);
			if (userOperation instanceof FinishedRefactoringOperation) {
				handleFinishedRefactoring((FinishedRefactoringOperation)userOperation);
			}
		}
		populateResults();
	}

	private void handleFinishedRefactoring(FinishedRefactoringOperation finishedRefactoringOperation) {
		finishedAutomatedRefactoringTimestamp= finishedRefactoringOperation.getTime();
		resetRefactoringState();
	}

	private void handleStartedRefactoring(NewStartedRefactoringOperation startedRefactoringOperation) {
		isInsideAutomatedRefactoring= true;
		currentAutomatedRefactoringID= startedRefactoringOperation.getID();
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

	protected boolean isInsideAutomatedRefactoring() {
		return isInsideAutomatedRefactoring;
	}

	protected RefactoringKind getCurrentAutomatedRefactoringKind() {
		return currentAutomatedRefactoringKind;
	}

	protected String getCurrentAutomatedRefactoringID() {
		return currentAutomatedRefactoringID;
	}

	protected boolean shouldIgnoreInferredRefactoring(InferredRefactoringOperation inferredRefactoring) {
		//TODO: Note that undoing a rename that follows an extract is still counted as a manual refactoring.
		return isInsideAutomatedRefactoring || InferredRefactoringOperation.isRename(inferredRefactoring.getRefactoringKind()) &&
				Math.abs(inferredRefactoring.getTime() - finishedAutomatedRefactoringTimestamp) < renameAfterExtractThreshold &&
				inferredRefactoring.getArguments().get("OldName").equals(extractedEntityName);
	}

	protected boolean shouldIgnoreAutomatedRefactoring(FinishedRefactoringOperation finishedRefactoring) {
		return currentAutomatedRefactoringKind == null || finishedRefactoring.isTooSimple() || !finishedRefactoring.getSuccess();
	}

	protected void initialize() {
		result= new StringBuffer();
		extractedEntityName= null;
		finishedAutomatedRefactoringTimestamp= -1;
		resetRefactoringState();
	}

	private void resetRefactoringState() {
		isInsideAutomatedRefactoring= false;
		currentAutomatedRefactoringKind= null;
		currentAutomatedRefactoringID= "";
	}

	@Override
	protected boolean shouldMergeResults() {
		return true;
	}

	@Override
	protected boolean shouldOutputIndividualResults() {
		return false;
	}

	public static RefactoringKind getRefactoringKind(NewStartedRefactoringOperation startedRefactoringOperation) {
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

}
