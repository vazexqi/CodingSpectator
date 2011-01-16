/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.refactorings;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

import edu.illinois.codingspectator.codingtracker.operations.OperationSymbols;

/**
 * 
 * @author Stas Negara
 * 
 */
public class PerformedRefactoringOperation extends RefactoringOperation {

	public PerformedRefactoringOperation() {
		super();
	}

	public PerformedRefactoringOperation(RefactoringDescriptor refactoringDescriptor) {
		super(refactoringDescriptor);
	}

	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.REFACTORING_PERFORMED_SYMBOL;
	}

	@Override
	public String getDescription() {
		return "Performed refactoring";
	}

	@Override
	public void replay() throws CoreException {
		Refactoring refactoring= getInitializedRefactoring();
		PerformRefactoringOperation performRefactoringOperation= new PerformRefactoringOperation(refactoring, CheckConditionsOperation.ALL_CONDITIONS);
		performRefactoringOperation.run(null);
		if (performRefactoringOperation.getConditionStatus().hasFatalError()) {
			throw new RuntimeException("Failed to check preconditions of refactoring: " + refactoring.getName());
		}
		if (performRefactoringOperation.getValidationStatus().hasFatalError()) {
			throw new RuntimeException("Failed to validate refactoring: " + refactoring.getName());
		}
	}

}
