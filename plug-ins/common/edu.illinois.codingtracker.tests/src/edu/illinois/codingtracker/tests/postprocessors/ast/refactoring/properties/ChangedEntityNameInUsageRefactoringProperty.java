/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties;



/**
 * This class represents changing a variable's name in its usage.
 * 
 * @author Stas Negara
 * 
 */
public class ChangedEntityNameInUsageRefactoringProperty extends RefactoringProperty {


	private ChangedEntityNameInUsageRefactoringProperty() {

	}

	public ChangedEntityNameInUsageRefactoringProperty(String oldEntityName, String newEntityName) {
		addAttribute(RefactoringPropertyAttributes.OLD_ENTITY_NAME, oldEntityName);
		addAttribute(RefactoringPropertyAttributes.NEW_ENTITY_NAME, newEntityName);
	}

	@Override
	protected RefactoringProperty createFreshInstance() {
		return new ChangedEntityNameInUsageRefactoringProperty();
	}

}
