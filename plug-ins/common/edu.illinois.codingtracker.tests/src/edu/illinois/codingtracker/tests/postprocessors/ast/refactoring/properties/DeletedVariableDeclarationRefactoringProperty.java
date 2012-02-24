/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties;



/**
 * This class represents a deleted declaration of a variable.
 * 
 * @author Stas Negara
 * 
 */
public class DeletedVariableDeclarationRefactoringProperty extends RefactoringProperty {

	private final String variableName;


	public DeletedVariableDeclarationRefactoringProperty(String variableName) {
		this.variableName= variableName;
	}

	public String getVariableName() {
		return variableName;
	}

}
