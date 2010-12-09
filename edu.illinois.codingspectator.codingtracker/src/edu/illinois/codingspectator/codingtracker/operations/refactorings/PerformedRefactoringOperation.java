/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.refactorings;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

import edu.illinois.codingspectator.codingtracker.operations.OperationSymbols;

/**
 * 
 * @author Stas Negara
 * 
 */
public class PerformedRefactoringOperation extends RefactoringOperation {

	public PerformedRefactoringOperation(RefactoringDescriptor refactoringDescriptor) {
		super(refactoringDescriptor);
	}

	@Override
	protected String getOperationSymbol() {
		return OperationSymbols.REFACTORING_PERFORMED_SYMBOL;
	}

	@Override
	protected String getDebugMessage() {
		return "Performed refactoring: ";
	}

}
