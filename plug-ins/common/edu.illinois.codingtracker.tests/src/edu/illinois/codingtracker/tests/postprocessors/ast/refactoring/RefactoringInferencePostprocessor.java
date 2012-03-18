/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring;

import java.util.List;

import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.operations.ast.InferredRefactoringOperation;
import edu.illinois.codingtracker.operations.files.snapshoted.SnapshotedFileOperation;
import edu.illinois.codingtracker.tests.postprocessors.ast.ASTPostprocessor;


/**
 * This class infers refactorings and inserts them in the sequence with AST operations. TODO: Decide
 * if each kind of refactoring should be inferred by a separate class.
 * 
 * @author Stas Negara
 * 
 */
public class RefactoringInferencePostprocessor extends ASTPostprocessor {

	private long lastSnapshotTimestamp= -1;


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
		InferredRefactoringFactory.resetCurrentState();
		for (int i= 0; i < userOperations.size(); i++) {
			UserOperation userOperation= userOperations.get(i);
			if (shouldReplayAndRecord(userOperation)) {
				replayAndRecord(userOperation);
				if (shouldProcess(userOperation)) {
					InferredRefactoringOperation refactoringOperation= InferredRefactoringFactory.handleASTOperation((ASTOperation)userOperation);
					if (refactoringOperation != null) {
						record(refactoringOperation);
					}
				}
			} else {
				record(userOperation);
			}
		}
	}

	private boolean shouldReplayAndRecord(UserOperation userOperation) {
		if (userOperation instanceof SnapshotedFileOperation) {
			lastSnapshotTimestamp= userOperation.getTime();
		}
		return userOperation.getTime() != lastSnapshotTimestamp - 1;
	}

}
