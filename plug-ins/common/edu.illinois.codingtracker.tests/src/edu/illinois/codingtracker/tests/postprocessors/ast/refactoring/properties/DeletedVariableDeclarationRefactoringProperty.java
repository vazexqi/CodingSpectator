/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties;

import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.InferredRefactoring;



/**
 * This class represents a deleted declaration of a variable.
 * 
 * @author Stas Negara
 * 
 */
public class DeletedVariableDeclarationRefactoringProperty extends AtomicRefactoringProperty {


	public DeletedVariableDeclarationRefactoringProperty(String entityName, long entityNameNodeID, long enclosingClassNodeID,
															long activationTimestamp) {
		super(activationTimestamp);
		addAttribute(RefactoringPropertyAttributes.ENTITY_NAME, entityName);
		addAttribute(RefactoringPropertyAttributes.ENTITY_NAME_NODE_ID, entityNameNodeID);
		addAttribute(RefactoringPropertyAttributes.ENCLOSING_CLASS_NODE_ID, enclosingClassNodeID);
	}

	@Override
	public boolean doesMatch(InferredRefactoring containingRefactoring, RefactoringProperty anotherProperty) {
		boolean doesMatch= super.doesMatch(containingRefactoring, anotherProperty);
		//Account for the automated Eclipse Promote Temp that changes the name of the promoted local variable.
		if (anotherProperty instanceof AddedFieldDeclarationRefactoringProperty && !doesMatch) {
			return isVeryCloseButDistinct(this, (AddedFieldDeclarationRefactoringProperty)anotherProperty);
		}
		return doesMatch;
	}

}
