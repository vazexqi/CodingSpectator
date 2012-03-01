/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties;

import edu.illinois.codingtracker.tests.postprocessors.ast.move.NodeDescriptor;



/**
 * This class represents an AST node deleted from initialization of a local variable.
 * 
 * @author Stas Negara
 * 
 */
public class MovedFromVariableInitializationRefactoringProperty extends RefactoringProperty {


	public MovedFromVariableInitializationRefactoringProperty(NodeDescriptor movedNode, String entityName, long moveID) {
		addAttribute(RefactoringPropertyAttributes.MOVED_NODE, movedNode);
		addAttribute(RefactoringPropertyAttributes.ENTITY_NAME, entityName);
		addAttribute(RefactoringPropertyAttributes.MOVE_ID, moveID);
	}

}
