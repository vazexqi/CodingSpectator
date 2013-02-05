/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.transformation;

import java.util.LinkedList;
import java.util.List;

import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.operations.files.snapshoted.SnapshotedFileOperation;
import edu.illinois.codingtracker.operations.refactorings.FinishedRefactoringOperation;
import edu.illinois.codingtracker.operations.refactorings.NewStartedRefactoringOperation;
import edu.illinois.codingtracker.tests.postprocessors.ast.ASTPostprocessor;


/**
 * This class is similar to UnknownTransformationInferencePostprocessor, except that it does not
 * replay the sequences, since it uses only the information that is part of the recorded AST node
 * operations.
 * 
 * @author Stas Negara
 * 
 */
public class UnknownTransformationInferenceWithoutReplayPostprocessor extends ASTPostprocessor {

	private long lastSnapshotTimestamp= -1;

	private boolean isInsideAutomatedRefactoring;


	@Override
	protected String getRecordFileName() {
		return "codechanges.txt.inferred_ast_operations.with_move.with_inferred_refactorings";
	}

	@Override
	protected String getResultFilePostfix() {
		return ".with_inferred_unknown_transformations";
	}

	@Override
	protected void postprocess(List<UserOperation> userOperations) {
		initialize(userOperations);
		//Create a copy for iterating to avoid concurrent modification errors that appear when the unknown transformation
		//factory adds inferred transformations to the list.
		List<UserOperation> copyUserOperations= new LinkedList<UserOperation>();
		copyUserOperations.addAll(userOperations);
		for (UserOperation userOperation : copyUserOperations) {
			if (userOperation instanceof NewStartedRefactoringOperation) {
				isInsideAutomatedRefactoring= true;
			} else if (userOperation instanceof FinishedRefactoringOperation) {
				isInsideAutomatedRefactoring= false;
			}
			postprocessUserOperation(userOperation);
		}
		for (UserOperation userOperation : userOperations) {
			record(userOperation);
		}
	}

	private void postprocessUserOperation(UserOperation userOperation) {
		if (shouldReplay(userOperation)) {
			record(userOperation, true);
			if (!isInsideAutomatedRefactoring && shouldProcess(userOperation)) {
				InferredUnknownTransformationFactory.processOperation((ASTOperation)userOperation, null);
			}
		} else {
			record(userOperation, true);
		}
	}

	private void initialize(List<UserOperation> userOperations) {
		InferredUnknownTransformationFactory.setUserOperations(userOperations);
		isInsideAutomatedRefactoring= false;
	}

	private boolean shouldReplay(UserOperation userOperation) {
		if (userOperation instanceof SnapshotedFileOperation) {
			lastSnapshotTimestamp= userOperation.getTime();
		}
		return userOperation.getTime() != lastSnapshotTimestamp - 1;
	}

}
