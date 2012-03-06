/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties;



/**
 * This class represents an added declaration of a method.
 * 
 * @author Stas Negara
 * 
 */
public class AddedMethodDeclarationRefactoringProperty extends RefactoringProperty {


	private AddedMethodDeclarationRefactoringProperty() {

	}

	public AddedMethodDeclarationRefactoringProperty(String entityName, long entityNameNodeID, long destinationMethodID) {
		addAttribute(RefactoringPropertyAttributes.ENTITY_NAME, entityName);
		addAttribute(RefactoringPropertyAttributes.ENTITY_NAME_NODE_ID, entityNameNodeID);
		addAttribute(RefactoringPropertyAttributes.DESTINATION_METHOD_ID, destinationMethodID);
	}

	@Override
	protected RefactoringProperty createFreshInstance() {
		return new AddedMethodDeclarationRefactoringProperty();
	}

}
