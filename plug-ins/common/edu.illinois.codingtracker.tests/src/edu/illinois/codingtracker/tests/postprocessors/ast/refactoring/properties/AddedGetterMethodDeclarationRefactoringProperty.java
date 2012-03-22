/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties;



/**
 * This class represents an added declaration of a (presumably) getter method.
 * 
 * @author Stas Negara
 * 
 */
public class AddedGetterMethodDeclarationRefactoringProperty extends AtomicRefactoringProperty {


	public AddedGetterMethodDeclarationRefactoringProperty(String getterMethodName, long getterMethodID, long activationTimestamp) {
		super(activationTimestamp);
		addAttribute(RefactoringPropertyAttributes.GETTER_METHOD_NAME, getterMethodName);
		addAttribute(RefactoringPropertyAttributes.GETTER_METHOD_ID, getterMethodID);
	}

}
