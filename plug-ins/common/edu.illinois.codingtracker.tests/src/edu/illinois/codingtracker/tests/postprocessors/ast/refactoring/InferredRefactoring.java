/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring;

import java.util.Map;

import edu.illinois.codingtracker.operations.ast.InferredRefactoringOperation.RefactoringKind;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties.RefactoringProperty;


/**
 * 
 * @author Stas Negara
 * 
 */
public abstract class InferredRefactoring {

	public abstract RefactoringKind getKind();

	public abstract Map<String, String> getArguments();

	public abstract boolean isComplete();

	protected abstract boolean isDisabled();

	public abstract void disableProperties();

	public abstract boolean checkDisabled();

	public abstract boolean canBePart(RefactoringProperty refactoringProperty);

	/**
	 * Should not change this refactoring, but rather should return a new one, with this refactoring
	 * property added to it.
	 * 
	 * @param refactoringProperty
	 */
	public abstract InferredRefactoring addProperty(RefactoringProperty refactoringProperty);

}
