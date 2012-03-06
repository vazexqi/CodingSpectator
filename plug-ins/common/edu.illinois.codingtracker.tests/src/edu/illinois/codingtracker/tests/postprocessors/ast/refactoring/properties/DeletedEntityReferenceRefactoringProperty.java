/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties;



/**
 * This class represents a deleted reference to a variable.
 * 
 * @author Stas Negara
 * 
 */
public class DeletedEntityReferenceRefactoringProperty extends RefactoringProperty {


	private DeletedEntityReferenceRefactoringProperty() {

	}

	public DeletedEntityReferenceRefactoringProperty(String entityName, long entityNameNodeID, long parentID) {
		addAttribute(RefactoringPropertyAttributes.ENTITY_NAME, entityName);
		addAttribute(RefactoringPropertyAttributes.ENTITY_NAME_NODE_ID, entityNameNodeID);
		addAttribute(RefactoringPropertyAttributes.PARENT_ID, parentID);
	}

	@Override
	protected RefactoringProperty createFreshInstance() {
		return new DeletedEntityReferenceRefactoringProperty();
	}

}
