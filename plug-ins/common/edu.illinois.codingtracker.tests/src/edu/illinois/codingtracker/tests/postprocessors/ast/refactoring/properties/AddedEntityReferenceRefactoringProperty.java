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
public class AddedEntityReferenceRefactoringProperty extends RefactoringProperty {


	public AddedEntityReferenceRefactoringProperty(String entityName, long entityNameNodeID, long parentID, long activationTimestamp) {
		super(activationTimestamp);
		addAttribute(RefactoringPropertyAttributes.ENTITY_NAME, entityName);
		addAttribute(RefactoringPropertyAttributes.ENTITY_NAME_NODE_ID, entityNameNodeID);
		addAttribute(RefactoringPropertyAttributes.PARENT_ID, parentID);
	}

}
