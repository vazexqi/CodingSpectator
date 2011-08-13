/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.refactorings;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

import edu.illinois.codingtracker.operations.OperationSymbols;

/**
 * This operation is no longer recorded.
 * 
 * {@see NewStartedRefactoringOperation, FinishedRefactoringOperation}.
 * 
 * @author Stas Negara
 * 
 */
public class RedoneRefactoringOperation extends RefactoringOperation {

	public RedoneRefactoringOperation() {
		super();
	}

	public RedoneRefactoringOperation(RefactoringDescriptor refactoringDescriptor) {
		super(refactoringDescriptor);
	}

	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.REFACTORING_REDONE_SYMBOL;
	}

	@Override
	public String getDescription() {
		return "Redone refactoring";
	}

	@Override
	public void replayRefactoring(RefactoringDescriptor refactoringDescriptor) throws CoreException {
		if (!unperformedRefactorings.contains(getTime())) {
			getRefactoringUndoManager().performRedo(null, null);
		}
	}

}
