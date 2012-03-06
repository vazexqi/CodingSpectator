/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties;

import edu.illinois.codingtracker.tests.postprocessors.ast.move.NodeDescriptor;



/**
 * This class represents a moved AST node that is added as part of the code that is NOT a variable
 * declaration.
 * 
 * @author Stas Negara
 * 
 */
public class MovedToUsageRefactoringProperty extends RefactoringProperty {


	private MovedToUsageRefactoringProperty() {

	}

	public MovedToUsageRefactoringProperty(NodeDescriptor movedNode, long moveID, long parentID) {
		addAttribute(RefactoringPropertyAttributes.MOVED_NODE, movedNode);
		addAttribute(RefactoringPropertyAttributes.MOVE_ID, moveID);
		addAttribute(RefactoringPropertyAttributes.PARENT_ID, parentID);
	}

	@Override
	protected RefactoringProperty createFreshInstance() {
		return new MovedToUsageRefactoringProperty();
	}

}
