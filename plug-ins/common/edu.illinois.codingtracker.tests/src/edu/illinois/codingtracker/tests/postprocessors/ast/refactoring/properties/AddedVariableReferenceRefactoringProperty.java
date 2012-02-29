/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties;



/**
 * This class represents an added reference to a variable.
 * 
 * @author Stas Negara
 * 
 */
public class AddedVariableReferenceRefactoringProperty extends RefactoringProperty {


	public AddedVariableReferenceRefactoringProperty(String variableName, long parentID) {
		addAttribute(RefactoringPropertyAttributes.VARIABLE_NAME, variableName);
		addAttribute(RefactoringPropertyAttributes.PARENT_ID, parentID);
	}

}
