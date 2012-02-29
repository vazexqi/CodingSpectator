/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties;



/**
 * This class represents an added declaration of a variable.
 * 
 * @author Stas Negara
 * 
 */
public class AddedVariableDeclarationRefactoringProperty extends RefactoringProperty {


	public AddedVariableDeclarationRefactoringProperty(String variableName) {
		addAttribute(RefactoringPropertyAttributes.VARIABLE_NAME, variableName);
	}


}
