/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties;



/**
 * This class represents changing a method's name in its declaration.
 * 
 * @author Stas Negara
 * 
 */
public class ChangedMethodNameInDeclarationRefactoringProperty extends AtomicRefactoringProperty {


	public ChangedMethodNameInDeclarationRefactoringProperty(String oldEntityName, String newEntityName, long activationTimestamp) {
		super(activationTimestamp);
		addAttribute(RefactoringPropertyAttributes.OLD_ENTITY_NAME, oldEntityName);
		addAttribute(RefactoringPropertyAttributes.NEW_ENTITY_NAME, newEntityName);
	}

}
