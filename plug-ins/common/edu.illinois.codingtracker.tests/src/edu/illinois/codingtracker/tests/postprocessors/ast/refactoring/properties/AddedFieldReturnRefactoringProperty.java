/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties;



/**
 * This class represents an added return of a field to a (tentatively) getter method.
 * 
 * @author Stas Negara
 * 
 */
public class AddedFieldReturnRefactoringProperty extends AtomicRefactoringProperty {


	public AddedFieldReturnRefactoringProperty(String entityName, long entityNameNodeID, long getterMethodID, long activationTimestamp) {
		super(activationTimestamp);
		addAttribute(RefactoringPropertyAttributes.ENTITY_NAME, entityName);
		addAttribute(RefactoringPropertyAttributes.ENTITY_NAME_NODE_ID, entityNameNodeID);
		addAttribute(RefactoringPropertyAttributes.GETTER_METHOD_ID, getterMethodID);
	}

}
