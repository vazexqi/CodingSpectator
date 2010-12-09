/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.refactorings;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

import edu.illinois.codingspectator.codingtracker.recording.Symbols;

/**
 * 
 * @author Stas Negara
 * 
 * 
 */
public class UndoneRefactoringOperation extends RefactoringOperation {

	public UndoneRefactoringOperation(RefactoringDescriptor refactoringDescriptor) {
		super(refactoringDescriptor, Symbols.REFACTORING_UNDONE_SYMBOL, "Undone refactoring: ");
	}

}
