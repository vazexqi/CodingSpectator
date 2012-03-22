/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties;



/**
 * This class represents an added invocation of a setter method.
 * 
 * @author Stas Negara
 * 
 */
public class AddedSetterMethodInvocationRefactoringProperty extends AtomicRefactoringProperty {


	public AddedSetterMethodInvocationRefactoringProperty(String setterMethodName, long parentID, long activationTimestamp) {
		super(activationTimestamp);
		addAttribute(RefactoringPropertyAttributes.SETTER_METHOD_NAME, setterMethodName);
		addAttribute(RefactoringPropertyAttributes.PARENT_ID, parentID);
	}

}
