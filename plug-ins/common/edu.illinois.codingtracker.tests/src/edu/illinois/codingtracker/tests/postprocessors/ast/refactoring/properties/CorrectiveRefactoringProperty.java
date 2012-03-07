/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties;



/**
 * This is a special case of a refactoring property that is not used separately, but instead, is
 * used to update the already accumulated refactoring properties.
 * 
 * @author Stas Negara
 * 
 */
public class CorrectiveRefactoringProperty extends RefactoringProperty {


	private CorrectiveRefactoringProperty() {

	}

	public CorrectiveRefactoringProperty(String entityName, long entityNameNodeID, String newEntityName) {
		addAttribute(RefactoringPropertyAttributes.ENTITY_NAME, entityName);
		addAttribute(RefactoringPropertyAttributes.ENTITY_NAME_NODE_ID, entityNameNodeID);
		addAttribute(RefactoringPropertyAttributes.NEW_ENTITY_NAME, newEntityName);
	}

	@Override
	protected RefactoringProperty createFreshInstance() {
		return new CorrectiveRefactoringProperty();
	}

	@Override
	protected boolean isIgnoredAttribute(String attribute) {
		return attribute.equals(RefactoringPropertyAttributes.NEW_ENTITY_NAME);
	}

	public boolean doesOverlap(RefactoringProperty anotherProperty) {
		return anotherProperty.getAttribute(RefactoringPropertyAttributes.ENTITY_NAME) != null &&
				anotherProperty.getAttribute(RefactoringPropertyAttributes.ENTITY_NAME_NODE_ID) != null;
	}

	public void correct(RefactoringProperty correctedProperty) {
		if (!doesOverlap(correctedProperty)) {
			throw new RuntimeException("Can not correct non-overlapping property: " + correctedProperty);
		}
		String correctedName= (String)correctedProperty.getAttribute(RefactoringPropertyAttributes.ENTITY_NAME);
		if (!correctedName.equals(getAttribute(RefactoringPropertyAttributes.ENTITY_NAME))) {
			throw new RuntimeException("Can not correct a property with a non-matching entity name: " + correctedName);
		}
		correctedProperty.addAttribute(RefactoringPropertyAttributes.ENTITY_NAME, getAttribute(RefactoringPropertyAttributes.NEW_ENTITY_NAME));
	}

}
