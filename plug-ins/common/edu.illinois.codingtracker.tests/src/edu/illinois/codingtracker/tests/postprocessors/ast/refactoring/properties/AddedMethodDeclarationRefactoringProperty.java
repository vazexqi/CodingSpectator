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


	public AddedMethodDeclarationRefactoringProperty(String destinationMethodName, long destinationMethodID) {
		addAttribute(RefactoringPropertyAttributes.DESTINATION_METHOD_NAME, destinationMethodName);
		addAttribute(RefactoringPropertyAttributes.DESTINATION_METHOD_ID, destinationMethodID);
	}

}
