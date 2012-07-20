/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties;



/**
 * This class represents changing a global (i.e., above method-level) etity's name in its usage.
 * 
 * @author Stas Negara
 * 
 */
public class ChangedEntityNameInUsageRefactoringProperty extends AtomicRefactoringProperty {


	public ChangedEntityNameInUsageRefactoringProperty(String oldEntityName, String newEntityName, long entityNameNodeID, String sourceMethodName, long activationTimestamp) {
		super(activationTimestamp);
		addAttribute(RefactoringPropertyAttributes.OLD_ENTITY_NAME, oldEntityName);
		addAttribute(RefactoringPropertyAttributes.NEW_ENTITY_NAME, newEntityName);
		addAttribute(RefactoringPropertyAttributes.ENTITY_NAME_NODE_ID, entityNameNodeID);
		addAttribute(RefactoringPropertyAttributes.SOURCE_METHOD_NAME, sourceMethodName);
	}

	@Override
	public boolean doesAffectSameEntity(RefactoringProperty refactoringProperty) {
		return getAttribute(RefactoringPropertyAttributes.ENTITY_NAME_NODE_ID).equals(refactoringProperty.getAttribute(RefactoringPropertyAttributes.ENTITY_NAME_NODE_ID));
	}

}
