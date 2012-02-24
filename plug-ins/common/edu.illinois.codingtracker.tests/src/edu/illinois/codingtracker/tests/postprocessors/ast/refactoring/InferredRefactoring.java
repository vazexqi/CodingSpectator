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
public interface InferredRefactoring {

	public RefactoringKind getKind();

	public Map<String, String> getArguments();

	public boolean isComplete();

	public void disableProperties();

	public boolean checkDisabled();

	public boolean canBePart(RefactoringProperty refactoringProperty);

	/**
	 * Should not change this refactoring, but rather should return a new one, with this refactoring
	 * property added to it.
	 * 
	 * @param refactoringProperty
	 */
	public InferredRefactoring addProperty(RefactoringProperty refactoringProperty);

}
