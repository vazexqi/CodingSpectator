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
public class MovedToFieldInitializationRefactoringProperty extends RefactoringProperty {


	public MovedToFieldInitializationRefactoringProperty(NodeDescriptor movedNode, String entityName, long moveID) {
		addAttribute(RefactoringPropertyAttributes.MOVED_NODE, movedNode);
		addAttribute(RefactoringPropertyAttributes.ENTITY_NAME, entityName);
		addAttribute(RefactoringPropertyAttributes.MOVE_ID, moveID);
	}

}
