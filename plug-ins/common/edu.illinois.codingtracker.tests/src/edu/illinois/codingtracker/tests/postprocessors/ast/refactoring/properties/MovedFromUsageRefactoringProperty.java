/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties;

import edu.illinois.codingtracker.tests.postprocessors.ast.move.NodeDescriptor;



/**
 * This class represents an AST node that is deleted and moved to other place.
 * 
 * @author Stas Negara
 * 
 */
public class MovedFromUsageRefactoringProperty extends AtomicRefactoringProperty {


	public MovedFromUsageRefactoringProperty(NodeDescriptor movedNode, long moveID, long parentID, long activationTimestamp) {
		super(activationTimestamp);
		addAttribute(RefactoringPropertyAttributes.MOVED_NODE, movedNode);
		addAttribute(RefactoringPropertyAttributes.MOVE_ID, moveID);
		addAttribute(RefactoringPropertyAttributes.PARENT_ID, parentID);
	}

}
