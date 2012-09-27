/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import edu.illinois.codingtracker.helpers.ResourceHelper;
import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.operations.ast.InferredRefactoringOperation;
import edu.illinois.codingtracker.operations.ast.InferredRefactoringOperation.RefactoringKind;
import edu.illinois.codingtracker.operations.refactorings.NewStartedRefactoringOperation;
import edu.illinois.codingtracker.recording.TextRecorder;
import edu.illinois.codingtracker.tests.analyzers.ast.refactoring.InferredRefactoringAnalyzer;
import edu.illinois.codingtracker.tests.postprocessors.CodingTrackerPostprocessor;

/**
 * This postprocessor removes spurious inferred Rename refactorings that precede an automated Rename
 * refactoring due to the way Eclipse handles in-place renames via linked edit boxes. Also, this
 * postprocessor unlinks the contributing AST operations of the removed refactorings by assigning -1
 * to their refactoring IDs.
 * 
 * !!!Note that this postprocessor updates the original files.
 * 
 * @author Stas Negara
 * 
 */
public class FilterSpuriousPrecedingInferredRenameRefactoringsPostprocessor extends CodingTrackerPostprocessor {

	private static int deletedRefactoringsCount= 0;

	private static final long renameBeforeAutomatedRenameThreshold= 15 * 1000; //15 seconds.

	private final List<InferredRefactoringOperation> renameRefactorings= new LinkedList<InferredRefactoringOperation>();

	private final Set<Long> refactoringIDsToDelete= new HashSet<Long>();


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
		update(userOperations);
		record(userOperations);
	}

	private void record(List<UserOperation> userOperations) {
		for (UserOperation userOperation : userOperations) {
			TextRecorder.record(userOperation);
		}
	}

	private void update(List<UserOperation> userOperations) {
		Iterator<UserOperation> userOperationsIterator= userOperations.iterator();
		while (userOperationsIterator.hasNext()) {
			UserOperation userOperation= userOperationsIterator.next();
			if (userOperation instanceof ASTOperation) {
				ASTOperation astOperation= (ASTOperation)userOperation;
				if (refactoringIDsToDelete.contains(astOperation.getTransformationID())) {
					astOperation.setTransformationID(-1);
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
			if (userOperation instanceof InferredRefactoringOperation) {
				postprocessInferredRefactoring((InferredRefactoringOperation)userOperation);
			} else if (userOperation instanceof NewStartedRefactoringOperation) {
				postprocessNewStartedRefactoringOperation((NewStartedRefactoringOperation)userOperation);
			}
		}
	}

	private void postprocessNewStartedRefactoringOperation(NewStartedRefactoringOperation refactoringOperation) {
		if (isRename(InferredRefactoringAnalyzer.getRefactoringKind(refactoringOperation))) {
			while (renameRefactorings.size() >= 2) {
				int lastIndex= renameRefactorings.size() - 1;
				InferredRefactoringOperation lastInferredRefactoringOperation= renameRefactorings.get(lastIndex);
				if (areCorrelated(refactoringOperation, lastInferredRefactoringOperation, true)) {
					InferredRefactoringOperation beforeLastInferredRefactoringOperation= renameRefactorings.get(lastIndex - 1);
					if (areCorrelated(refactoringOperation, beforeLastInferredRefactoringOperation, false) &&
							areInverseRenames(lastInferredRefactoringOperation, beforeLastInferredRefactoringOperation)) {
						refactoringIDsToDelete.add(lastInferredRefactoringOperation.getRefactoringID());
						refactoringIDsToDelete.add(beforeLastInferredRefactoringOperation.getRefactoringID());
						renameRefactorings.remove(lastIndex);
						renameRefactorings.remove(lastIndex - 1);
						continue;
					}
				}
				break;
			}
		}
		renameRefactorings.clear(); //Should keep only inferred refactorings that immediately precede an automated refactoring.
	}

	private boolean areInverseRenames(InferredRefactoringOperation inferredRefactoringOperation1, InferredRefactoringOperation inferredRefactoringOperation2) {
		return inferredRefactoringOperation1.getArguments().get("OldName").equals(inferredRefactoringOperation2.getArguments().get("NewName")) &&
				inferredRefactoringOperation1.getArguments().get("NewName").equals(inferredRefactoringOperation2.getArguments().get("OldName"));
	}

	private boolean areCorrelated(NewStartedRefactoringOperation automatedRefactoringOperation, InferredRefactoringOperation inferredRefactoringOperation, boolean isUndo) {
		String automatedName= automatedRefactoringOperation.getArguments().get("name");
		String inferredName= removeParentheses(inferredRefactoringOperation.getArguments().get(isUndo ? "OldName" : "NewName"));
		return inferredRefactoringOperation.getRefactoringKind() == InferredRefactoringAnalyzer.getRefactoringKind(automatedRefactoringOperation) &&
				Math.abs(inferredRefactoringOperation.getTime() - automatedRefactoringOperation.getTime()) < renameBeforeAutomatedRenameThreshold &&
				inferredName.equals(automatedName);
	}

	/**
	 * It is used to remove the parentheses that follow a method name.
	 * 
	 * @param str
	 * @return
	 */
	private String removeParentheses(String str) {
		int openParenthesisIndex= str.indexOf("(");
		if (openParenthesisIndex != -1) {
			return str.substring(0, openParenthesisIndex);
		}
		return str;
	}

	private void postprocessInferredRefactoring(InferredRefactoringOperation inferredRefactoringOperation) {
		if (isRename(inferredRefactoringOperation.getRefactoringKind())) {
			renameRefactorings.add(inferredRefactoringOperation);
		}
	}

	private boolean isRename(RefactoringKind refactoringKind) {
		return refactoringKind == RefactoringKind.RENAME_CLASS || refactoringKind == RefactoringKind.RENAME_FIELD ||
				refactoringKind == RefactoringKind.RENAME_LOCAL_VARIABLE || refactoringKind == RefactoringKind.RENAME_METHOD;
	}

	private void initialize() {
		renameRefactorings.clear();
		refactoringIDsToDelete.clear();
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
