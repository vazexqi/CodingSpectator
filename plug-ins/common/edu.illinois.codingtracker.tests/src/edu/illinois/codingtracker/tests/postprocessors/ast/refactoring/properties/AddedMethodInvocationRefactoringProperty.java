/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties;



/**
 * This class represents an added invocation of a method.
 * 
 * @author Stas Negara
 * 
 */
public class AddedMethodInvocationRefactoringProperty extends RefactoringProperty {


	public AddedMethodInvocationRefactoringProperty(String destinationMethodName, String sourceMethodName, long sourceMethodID) {
		addAttribute(RefactoringPropertyAttributes.DESTINATION_METHOD_NAME, destinationMethodName);
		addAttribute(RefactoringPropertyAttributes.SOURCE_METHOD_NAME, sourceMethodName);
		addAttribute(RefactoringPropertyAttributes.SOURCE_METHOD_ID, sourceMethodID);
	}

}
