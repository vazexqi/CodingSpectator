/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties;



/**
 * This class represents an added invocation of a getter method.
 * 
 * @author Stas Negara
 * 
 */
public class AddedGetterMethodInvocationRefactoringProperty extends AtomicRefactoringProperty {


	public AddedGetterMethodInvocationRefactoringProperty(String getterMethodName, long parentID, long activationTimestamp) {
		super(activationTimestamp);
		addAttribute(RefactoringPropertyAttributes.GETTER_METHOD_NAME, getterMethodName);
		addAttribute(RefactoringPropertyAttributes.PARENT_ID, parentID);
	}

}
