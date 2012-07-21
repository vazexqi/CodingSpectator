/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties;



/**
 * This class represents changing a variable's name in its declaration.
 * 
 * @author Stas Negara
 * 
 */
public class ChangedVariableNameInDeclarationRefactoringProperty extends AtomicRefactoringProperty {


	public ChangedVariableNameInDeclarationRefactoringProperty(String oldEntityName, String newEntityName, long sourceMethodID, long activationTimestamp) {
		super(activationTimestamp);
		addAttribute(RefactoringPropertyAttributes.OLD_ENTITY_NAME, oldEntityName);
		addAttribute(RefactoringPropertyAttributes.NEW_ENTITY_NAME, newEntityName);
		addAttribute(RefactoringPropertyAttributes.SOURCE_METHOD_ID, sourceMethodID);
	}

}
