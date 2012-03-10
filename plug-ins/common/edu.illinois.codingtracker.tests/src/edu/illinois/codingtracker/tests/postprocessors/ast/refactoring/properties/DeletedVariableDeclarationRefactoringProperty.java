/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties;



/**
 * This class represents a deleted declaration of a variable.
 * 
 * @author Stas Negara
 * 
 */
public class DeletedVariableDeclarationRefactoringProperty extends RefactoringProperty {


	public DeletedVariableDeclarationRefactoringProperty(String entityName, long entityNameNodeID, long activationTimestamp) {
		super(activationTimestamp);
		addAttribute(RefactoringPropertyAttributes.ENTITY_NAME, entityName);
		addAttribute(RefactoringPropertyAttributes.ENTITY_NAME_NODE_ID, entityNameNodeID);
	}

	@Override
	public boolean doesMatch(RefactoringProperty anotherProperty) {
		boolean doesMatch= super.doesMatch(anotherProperty);
		//Account for the automated Eclipse Promote Temp that changes the name of the promoted local variable.
		if (anotherProperty instanceof AddedFieldDeclarationRefactoringProperty && !doesMatch) {
			return isVeryCloseButDistinct(this, anotherProperty);
		}
		return doesMatch;
	}

}
