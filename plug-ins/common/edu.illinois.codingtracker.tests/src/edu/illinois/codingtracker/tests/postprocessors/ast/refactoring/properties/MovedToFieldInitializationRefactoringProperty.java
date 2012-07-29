/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties;

import edu.illinois.codingtracker.tests.postprocessors.ast.move.NodeDescriptor;



/**
 * This class represents an AST node added as initialization of a field.
 * 
 * @author Stas Negara
 * 
 */
public class MovedToFieldInitializationRefactoringProperty extends AtomicRefactoringProperty {


	public MovedToFieldInitializationRefactoringProperty(NodeDescriptor movedNode, String entityName, long entityNameNodeID,
															long moveID, long enclosingClassNodeID, long activationTimestamp) {
		super(activationTimestamp);
		addAttribute(RefactoringPropertyAttributes.MOVED_NODE, movedNode);
		addAttribute(RefactoringPropertyAttributes.ENTITY_NAME, entityName);
		addAttribute(RefactoringPropertyAttributes.ENTITY_NAME_NODE_ID, entityNameNodeID);
		addAttribute(RefactoringPropertyAttributes.MOVE_ID, moveID);
		addAttribute(RefactoringPropertyAttributes.ENCLOSING_CLASS_NODE_ID, enclosingClassNodeID);
	}

}
