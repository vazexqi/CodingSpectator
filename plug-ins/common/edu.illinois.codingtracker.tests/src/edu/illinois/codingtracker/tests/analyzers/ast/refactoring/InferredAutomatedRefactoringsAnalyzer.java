/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers.ast.refactoring;

import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.ast.InferredRefactoringOperation;
import edu.illinois.codingtracker.operations.ast.InferredRefactoringOperation.RefactoringKind;
import edu.illinois.codingtracker.operations.refactorings.FinishedRefactoringOperation;
import edu.illinois.codingtracker.operations.refactorings.NewStartedRefactoringOperation;


/**
 * This analyzer reports potentially not inferred automated refactorings.
 * 
 * @author Stas Negara
 * 
 */
public class InferredAutomatedRefactoringsAnalyzer extends InferredRefactoringAnalyzer {

	private boolean isCurrentAutomatedRefactoringInferred= false;

	private int totalAutomatedRefactoringsCount= 0;

	private int totalUninferredAutomatedRefactoringsCount= 0;


	@Override
	protected String getTableHeader() {
		return "username,workspace ID,version\n";
	}

	@Override
	protected void postprocessOperation(UserOperation userOperation) {
		if (userOperation instanceof InferredRefactoringOperation &&
				((InferredRefactoringOperation)userOperation).getRefactoringKind() == getCurrentAutomatedRefactoringKind()) {
			isCurrentAutomatedRefactoringInferred= true;
		}
		if (userOperation instanceof NewStartedRefactoringOperation) {
			isCurrentAutomatedRefactoringInferred= false;
			if (getCurrentAutomatedRefactoringKind() != null) {
				totalAutomatedRefactoringsCount++;
			}
		}
		if (userOperation instanceof FinishedRefactoringOperation && getCurrentAutomatedRefactoringKind() != null) {
			FinishedRefactoringOperation finishedRefactoringOperation= (FinishedRefactoringOperation)userOperation;
			if (!finishedRefactoringOperation.getSuccess() || finishedRefactoringOperation.isTooSimple() ||
						getCurrentAutomatedRefactoringKind() == RefactoringKind.ENCAPSULATE_FIELD) {
				totalAutomatedRefactoringsCount--;
			} else if (!isCurrentAutomatedRefactoringInferred) {
				totalUninferredAutomatedRefactoringsCount++;
				System.out.println("Timestamp: " + userOperation.getTime());
			}
		}
	}

	@Override
	protected void initialize() {
		super.initialize();
	}

	@Override
	protected void populateResults() {
		//no results
	}

	@Override
	protected void finishedProcessingAllSequences() {
		System.out.println("Total automated refactorings: " + totalAutomatedRefactoringsCount);
		System.out.println("Total uninferred automated refactorings: " + totalUninferredAutomatedRefactoringsCount);
	}

	@Override
	protected String getResultFilePostfix() {
		return ".inferred_automated_refactorings";
	}

}
