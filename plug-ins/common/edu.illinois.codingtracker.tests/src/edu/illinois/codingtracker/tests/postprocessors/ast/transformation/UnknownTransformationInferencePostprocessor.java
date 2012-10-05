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
 * This class infers unknown transformations and inserts them in the sequence with AST operations
 * and known transformations (including refactorings).
 * 
 * @author Stas Negara
 * 
 */
public class UnknownTransformationInferencePostprocessor extends ASTPostprocessor {

	private long lastSnapshotTimestamp= -1;

	private boolean isInsideAutomatedRefactoring;

	private static long lastProcessedTimestamp;


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
		//Process the remaining cache and record the resulting sequence.
		InferredUnknownTransformationFactory.processCachedOperations();
		for (UserOperation userOperation : userOperations) {
			record(userOperation);
		}
	}

	private void postprocessUserOperation(UserOperation userOperation) {
		if (userOperation.getTime() != lastProcessedTimestamp) {
			InferredUnknownTransformationFactory.processCachedOperations();
			lastProcessedTimestamp= userOperation.getTime();
		}
		if (shouldReplay(userOperation)) {
			replayAndRecord(userOperation, true);
			if (!isInsideAutomatedRefactoring && shouldProcess(userOperation)) {
				InferredUnknownTransformationFactory.handleASTOperation((ASTOperation)userOperation);
			}
		} else {
			record(userOperation, true);
		}
	}

	private void initialize(List<UserOperation> userOperations) {
		InferredUnknownTransformationFactory.resetCurrentState(userOperations);
		isInsideAutomatedRefactoring= false;
		lastProcessedTimestamp= -1;
	}

	private boolean shouldReplay(UserOperation userOperation) {
		if (userOperation instanceof SnapshotedFileOperation) {
			lastSnapshotTimestamp= userOperation.getTime();
		}
		return userOperation.getTime() != lastSnapshotTimestamp - 1;
	}

}
