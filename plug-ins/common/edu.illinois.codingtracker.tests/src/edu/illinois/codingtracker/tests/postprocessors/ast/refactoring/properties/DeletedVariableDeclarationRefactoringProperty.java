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


	private DeletedVariableDeclarationRefactoringProperty() {

	}

	public DeletedVariableDeclarationRefactoringProperty(String entityName, long entityNameNodeID) {
		addAttribute(RefactoringPropertyAttributes.ENTITY_NAME, entityName);
		addAttribute(RefactoringPropertyAttributes.ENTITY_NAME_NODE_ID, entityNameNodeID);
	}

	@Override
	protected RefactoringProperty createFreshInstance() {
		return new DeletedVariableDeclarationRefactoringProperty();
	}

}
