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
public class AddedFieldReturnRefactoringProperty extends RefactoringProperty {


	public AddedFieldReturnRefactoringProperty(String entityName, long getterMethodID) {
		addAttribute(RefactoringPropertyAttributes.ENTITY_NAME, entityName);
		addAttribute(RefactoringPropertyAttributes.GETTER_METHOD_ID, getterMethodID);
	}

}
