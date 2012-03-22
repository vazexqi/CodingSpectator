/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties;



/**
 * This class represents an added assignment of a field to a (tentatively) setter method.
 * 
 * @author Stas Negara
 * 
 */
public class AddedFieldAssignmentRefactoringProperty extends AtomicRefactoringProperty {


	public AddedFieldAssignmentRefactoringProperty(String entityName, long entityNameNodeID, long setterMethodID, long activationTimestamp) {
		super(activationTimestamp);
		addAttribute(RefactoringPropertyAttributes.ENTITY_NAME, entityName);
		addAttribute(RefactoringPropertyAttributes.ENTITY_NAME_NODE_ID, entityNameNodeID);
		addAttribute(RefactoringPropertyAttributes.SETTER_METHOD_ID, setterMethodID);
	}

}
