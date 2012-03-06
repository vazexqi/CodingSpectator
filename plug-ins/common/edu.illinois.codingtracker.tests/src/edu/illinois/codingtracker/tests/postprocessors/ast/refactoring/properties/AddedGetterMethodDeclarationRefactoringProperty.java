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
public class AddedGetterMethodDeclarationRefactoringProperty extends RefactoringProperty {


	private AddedGetterMethodDeclarationRefactoringProperty() {

	}

	public AddedGetterMethodDeclarationRefactoringProperty(String getterMethodName, long getterMethodID) {
		addAttribute(RefactoringPropertyAttributes.GETTER_METHOD_NAME, getterMethodName);
		addAttribute(RefactoringPropertyAttributes.GETTER_METHOD_ID, getterMethodID);
	}

	@Override
	protected RefactoringProperty createFreshInstance() {
		return new AddedGetterMethodDeclarationRefactoringProperty();
	}

}
