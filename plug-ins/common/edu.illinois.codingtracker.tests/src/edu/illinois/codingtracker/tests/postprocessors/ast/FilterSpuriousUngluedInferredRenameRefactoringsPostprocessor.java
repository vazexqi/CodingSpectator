/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.illinois.codingtracker.helpers.ResourceHelper;
import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.operations.ast.InferredRefactoringOperation;
import edu.illinois.codingtracker.operations.ast.InferredRefactoringOperation.RefactoringKind;
import edu.illinois.codingtracker.operations.refactorings.FinishedRefactoringOperation;
import edu.illinois.codingtracker.operations.refactorings.NewStartedRefactoringOperation;
import edu.illinois.codingtracker.recording.TextRecorder;
import edu.illinois.codingtracker.tests.postprocessors.CodingTrackerPostprocessor;

/**
 * This postprocessor removes spurious inferred Rename refactorings that are produced by gluing
 * failure, when each incremental entity name change results into a separate inferred Rename
 * refactoring. This postprocessor also removes the contributing AST operations of the spurious
 * inferred refactorings and updates the remaining refactoring such that it reflects the full
 * change, i.e., it glues all spurious refactorings into a single one.
 * 
 * !!!Note that this postprocessor updates the original files. Also, it should be applied after the
 * other post-refactoring-inference postprocessors.
 * 
 * @author Stas Negara
 * 
 */
public class FilterSpuriousUngluedInferredRenameRefactoringsPostprocessor extends CodingTrackerPostprocessor {

	private static int deletedRefactoringsCount= 0;

	private static final long ungluedRenameThreshold= 3 * 1000; //3 seconds.

	private final Map<Long, Set<Long>> refactoredNodeIDsMap= new HashMap<Long, Set<Long>>();

	private final Set<Long> refactoringIDsToDelete= new HashSet<Long>();

	private InferredRefactoringOperation currentInferredRefactoringOperation= null;


	@Override
	protected void checkPostprocessingPreconditions() {
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
		analyze(userOperations);
		removeFilteredOutOperations(userOperations);
		record(userOperations);
	}

	private void record(List<UserOperation> userOperations) {
		for (UserOperation userOperation : userOperations) {
			TextRecorder.record(userOperation);
		}
	}

	private void removeFilteredOutOperations(List<UserOperation> userOperations) {
		Iterator<UserOperation> userOperationsIterator= userOperations.iterator();
		while (userOperationsIterator.hasNext()) {
			UserOperation userOperation= userOperationsIterator.next();
			if (userOperation instanceof ASTOperation) {
				ASTOperation astOperation= (ASTOperation)userOperation;
				if (refactoringIDsToDelete.contains(astOperation.getTransformationID())) {
					userOperationsIterator.remove();
				}
			} else if (userOperation instanceof InferredRefactoringOperation) {
				InferredRefactoringOperation inferredRefactoringOperation= (InferredRefactoringOperation)userOperation;
				if (refactoringIDsToDelete.contains(inferredRefactoringOperation.getRefactoringID())) {
					userOperationsIterator.remove();
					deletedRefactoringsCount++;
					System.out.println("Removed inferred refactoring timestamp: " + inferredRefactoringOperation.getTime());
				}
			}
		}
	}

	private void analyze(List<UserOperation> userOperations) {
		for (UserOperation userOperation : userOperations) {
			if (userOperation instanceof ASTOperation) {
				postprocessASTOperation((ASTOperation)userOperation);
			} else if (userOperation instanceof InferredRefactoringOperation) {
				postprocessInferredRefactoring((InferredRefactoringOperation)userOperation);
			} else if (userOperation instanceof NewStartedRefactoringOperation ||
						userOperation instanceof FinishedRefactoringOperation) {
				currentInferredRefactoringOperation= null;
			}
		}
	}

	private void postprocessASTOperation(ASTOperation operation) {
		long refactoringID= operation.getTransformationID();
		if (refactoringID != -1) {
			Set<Long> refactoredNodeIDs= refactoredNodeIDsMap.get(refactoringID);
			if (refactoredNodeIDs == null) {
				refactoredNodeIDs= new HashSet<Long>();
				refactoredNodeIDsMap.put(refactoringID, refactoredNodeIDs);
			}
			refactoredNodeIDs.add(operation.getNodeID());
		}
	}

	private void postprocessInferredRefactoring(InferredRefactoringOperation inferredRefactoringOperation) {
		if (isContinuingRename(inferredRefactoringOperation)) {
			inferredRefactoringOperation.getArguments().put("OldName", currentInferredRefactoringOperation.getArguments().get("OldName"));
			refactoringIDsToDelete.add(currentInferredRefactoringOperation.getRefactoringID());
			currentInferredRefactoringOperation= inferredRefactoringOperation;
		} else if (isRename(inferredRefactoringOperation.getRefactoringKind())) {
			currentInferredRefactoringOperation= inferredRefactoringOperation;
		} else {
			currentInferredRefactoringOperation= null;
		}
	}

	private boolean isContinuingRename(InferredRefactoringOperation inferredRefactoringOperation) {
		if (currentInferredRefactoringOperation == null) {
			return false;
		}
		return Math.abs(inferredRefactoringOperation.getTime() - currentInferredRefactoringOperation.getTime()) < ungluedRenameThreshold &&
				currentInferredRefactoringOperation.getRefactoringKind() == inferredRefactoringOperation.getRefactoringKind() &&
				refactoredNodeIDsMap.get(currentInferredRefactoringOperation.getRefactoringID()).equals(refactoredNodeIDsMap.get(inferredRefactoringOperation.getRefactoringID())) &&
				currentInferredRefactoringOperation.getArguments().get("NewName").equals(inferredRefactoringOperation.getArguments().get("OldName")) &&
				//Should not be undoing since undoing should be counted as a separate refactoring.
				!currentInferredRefactoringOperation.getArguments().get("OldName").equals(inferredRefactoringOperation.getArguments().get("NewName"));
	}

	private boolean isRename(RefactoringKind refactoringKind) {
		return refactoringKind == RefactoringKind.RENAME_CLASS || refactoringKind == RefactoringKind.RENAME_FIELD ||
				refactoringKind == RefactoringKind.RENAME_LOCAL_VARIABLE || refactoringKind == RefactoringKind.RENAME_METHOD;
	}

	private void initialize() {
		refactoredNodeIDsMap.clear();
		refactoringIDsToDelete.clear();
		currentInferredRefactoringOperation= null;
	}

	@Override
	protected void finishedProcessingAllSequences() {
		System.out.println("Total deleted refactorings count: " + deletedRefactoringsCount);
	}

	@Override
	protected String getResultFilePostfix() {
		return ""; //Write back to the source file.
	}

	@Override
	protected String getResult() {
		return ResourceHelper.readFileContent(mainRecordFile);
	}

}
