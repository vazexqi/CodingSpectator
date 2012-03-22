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
public class AddedSetterMethodDeclarationRefactoringProperty extends AtomicRefactoringProperty {


	public AddedSetterMethodDeclarationRefactoringProperty(String setterMethodName, long setterMethodID, long activationTimestamp) {
		super(activationTimestamp);
		addAttribute(RefactoringPropertyAttributes.SETTER_METHOD_NAME, setterMethodName);
		addAttribute(RefactoringPropertyAttributes.SETTER_METHOD_ID, setterMethodID);
	}

}
