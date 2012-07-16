/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring;

import java.util.LinkedList;
import java.util.List;

import edu.illinois.codingtracker.helpers.StringHelper;
import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.operations.files.snapshoted.SnapshotedFileOperation;
import edu.illinois.codingtracker.operations.refactorings.FinishedRefactoringOperation;
import edu.illinois.codingtracker.operations.refactorings.NewStartedRefactoringOperation;
import edu.illinois.codingtracker.operations.textchanges.TextChangeOperation;
import edu.illinois.codingtracker.tests.postprocessors.ast.ASTPostprocessor;


/**
 * This class infers refactorings and inserts them in the sequence with AST operations. Also, it
 * establishes whether automated refactorings are too simple to be inferred/counted.
 * 
 * @author Stas Negara
 * 
 */
public class RefactoringInferencePostprocessor extends ASTPostprocessor {

	private long lastSnapshotTimestamp= -1;

	private boolean isInsideAutomatedRefactoring;

	private String affectedAutomatedRefactoringFile;

	private int affectedAutomatedRefactoringLineNumber;

	private boolean isAutomatedRefactoringTooSimple;

	private UserOperation lastProcessedUserOperation;

	//TODO: This flag is used to improve scalability of the inference of Encapsulate Field refactoring. Probably, there
	//is a more elegant way to achieve this goal.
	public static boolean isIntroducingGetterInvocation= false;

	//TODO: Ignore inference for these refactorings to make it scalable. In future, find a more elegant way for this.
	public boolean isInsideEncapsulateFieldAutomatedRefactoring;


	@Override
	protected String getRecordFileName() {
		return "codechanges.txt.inferred_ast_operations.with_move";
	}

	@Override
	protected String getResultFilePostfix() {
		return ".with_inferred_refactorings";
	}

	@Override
	protected void postprocess(List<UserOperation> userOperations) {
		initialize(userOperations);
		//Create a copy for iterating to avoid concurrent modification errors that appear when the refactoring factory
		//adds inferred refactorings to the list.
		List<UserOperation> copyUserOperations= new LinkedList<UserOperation>();
		copyUserOperations.addAll(userOperations);
		for (UserOperation userOperation : copyUserOperations) {
			if (userOperation instanceof NewStartedRefactoringOperation) {
				handleStartedRefactoring((NewStartedRefactoringOperation)userOperation);
			} else if (userOperation instanceof FinishedRefactoringOperation) {
				handleFinishedRefactoring((FinishedRefactoringOperation)userOperation);
			} else if (isInsideAutomatedRefactoring && isAutomatedRefactoringTooSimple &&
						userOperation instanceof TextChangeOperation) {
				handleTextChangeInsideTooSimpleRefactoring((TextChangeOperation)userOperation);
			}
			postprocessUserOperation(userOperation);
		}
		InferredRefactoringFactory.flushCompleteRefactorings();
		//Inference is finished, record the resulting sequence.
		for (UserOperation userOperation : userOperations) {
			record(userOperation);
		}
	}

	private void postprocessUserOperation(UserOperation userOperation) {
		detectIntroducingGetterMethod(userOperation);
		if (shouldReplay(userOperation)) {
			replayAndRecord(userOperation, true);
			if (!isInsideEncapsulateFieldAutomatedRefactoring && shouldProcess(userOperation)) {
				InferredRefactoringFactory.handleASTOperation((ASTOperation)userOperation);
			}
		} else {
			record(userOperation, true);
		}
		lastProcessedUserOperation= userOperation;
	}

	private void detectIntroducingGetterMethod(UserOperation userOperation) {
		if (userOperation instanceof TextChangeOperation && !(lastProcessedUserOperation instanceof TextChangeOperation)) {
			TextChangeOperation textChangeOperation= (TextChangeOperation)userOperation;
			String replacedText= textChangeOperation.getReplacedText();
			String newText= textChangeOperation.getNewText();
			if (newText.equals("get" + StringHelper.capitalizeFirstCharacter(replacedText) + "()")) {
				isIntroducingGetterInvocation= true;
				return;
			}
		}
		if (!(userOperation instanceof ASTOperation)) {
			isIntroducingGetterInvocation= false;
		}
	}

	private void handleTextChangeInsideTooSimpleRefactoring(TextChangeOperation textChangeOperation) {
		if (shouldReplay(textChangeOperation)) { //Consider only those operations that do not represent effects of snapshots.
			String editedFilePath= textChangeOperation.getEditedFilePath();
			int[] affectedLineNumbers= textChangeOperation.getAffectedLineNumbers();
			if (affectedLineNumbers.length > 0) {
				if (affectedAutomatedRefactoringFile == null) {
					affectedAutomatedRefactoringFile= editedFilePath;
				}
				if (affectedAutomatedRefactoringLineNumber == -1) {
					affectedAutomatedRefactoringLineNumber= affectedLineNumbers[0];
				}
				//The heuristic is that a refactoring affecting a single line of a single file is too simple.
				if (!affectedAutomatedRefactoringFile.equals(editedFilePath) || affectedLineNumbers.length > 1 ||
						affectedAutomatedRefactoringLineNumber != affectedLineNumbers[0]) {
					isAutomatedRefactoringTooSimple= false;
				}
			}
		}
	}

	private void handleFinishedRefactoring(FinishedRefactoringOperation finishedRefactoringOperation) {
		finishedRefactoringOperation.setTooSimple(isAutomatedRefactoringTooSimple);
		resetRefactoringState();
	}

	private void handleStartedRefactoring(NewStartedRefactoringOperation startedRefactoringOperation) {
		isInsideAutomatedRefactoring= true;
		if (startedRefactoringOperation.getID().endsWith(".self.encapsulate")) {
			isInsideEncapsulateFieldAutomatedRefactoring= true;
		}
	}

	private void initialize(List<UserOperation> userOperations) {
		isIntroducingGetterInvocation= false;
		InferredRefactoringFactory.resetCurrentState();
		InferredRefactoringFactory.userOperations= userOperations;
		resetRefactoringState();
	}

	private void resetRefactoringState() {
		isInsideAutomatedRefactoring= false;
		isInsideEncapsulateFieldAutomatedRefactoring= false;
		affectedAutomatedRefactoringFile= null;
		affectedAutomatedRefactoringLineNumber= -1;
		isAutomatedRefactoringTooSimple= true;
	}

	private boolean shouldReplay(UserOperation userOperation) {
		if (userOperation instanceof SnapshotedFileOperation) {
			lastSnapshotTimestamp= userOperation.getTime();
		}
		return userOperation.getTime() != lastSnapshotTimestamp - 1;
	}

}
