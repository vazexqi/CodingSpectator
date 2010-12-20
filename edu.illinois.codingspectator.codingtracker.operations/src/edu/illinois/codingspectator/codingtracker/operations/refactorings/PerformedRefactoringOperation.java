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

}
