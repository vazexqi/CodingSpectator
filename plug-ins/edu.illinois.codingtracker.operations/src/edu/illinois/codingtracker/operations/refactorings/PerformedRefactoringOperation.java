/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.refactorings;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.core.refactoring.CheckConditionsOperation;
import org.eclipse.ltk.core.refactoring.PerformRefactoringOperation;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import edu.illinois.codingtracker.helpers.Debugger;
import edu.illinois.codingtracker.operations.OperationSymbols;

/**
 * This operation is no longer recorded.
 * 
 * {@see NewStartedRefactoringOperation, FinishedRefactoringOperation}.
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
	public void replayRefactoring(RefactoringDescriptor refactoringDescriptor) throws CoreException {
		try {
			//FIXME: This is a temporary hack. It is required to overcome the problem that sometimes Eclipse does not finish updating 
			//program's structure yet, and thus, the refactoring can not be properly initialized (i.e. the refactored element is not found).
			//Find a better solution, e.g. listen for some Eclipse "refreshing" process to complete.
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			//do nothing
		}
		RefactoringStatus initializationStatus= new RefactoringStatus();
		Refactoring refactoring= refactoringDescriptor.createRefactoring(initializationStatus);
		if (!initializationStatus.isOK()) {
			Debugger.debugWarning("Failed to initialize a refactoring from its descriptor: " + refactoringDescriptor);
			unperformedRefactorings.add(getTime());
			return;
		}
		//This remove is needed to ensure that multiple replays in the same run do not overlap
		unperformedRefactorings.remove(getTime());
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
