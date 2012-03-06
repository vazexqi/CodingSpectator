/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties;



/**
 * This class represents an added declaration of a (presumably) setter method.
 * 
 * @author Stas Negara
 * 
 */
public class AddedSetterMethodDeclarationRefactoringProperty extends RefactoringProperty {


	private AddedSetterMethodDeclarationRefactoringProperty() {

	}

	public AddedSetterMethodDeclarationRefactoringProperty(String setterMethodName, long setterMethodID) {
		addAttribute(RefactoringPropertyAttributes.SETTER_METHOD_NAME, setterMethodName);
		addAttribute(RefactoringPropertyAttributes.SETTER_METHOD_ID, setterMethodID);
	}

	@Override
	protected RefactoringProperty createFreshInstance() {
		return new AddedSetterMethodDeclarationRefactoringProperty();
	}

}
