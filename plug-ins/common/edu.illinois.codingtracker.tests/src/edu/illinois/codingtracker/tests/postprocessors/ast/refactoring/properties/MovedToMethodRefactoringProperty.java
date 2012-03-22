/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties;



/**
 * This class represents a moved AST node that is added to some method.
 * 
 * @author Stas Negara
 * 
 */
public class MovedToMethodRefactoringProperty extends AtomicRefactoringProperty {


	public MovedToMethodRefactoringProperty(long destinationMethodID, String entityName, long entityNameNodeID, long moveID, long activationTimestamp) {
		super(activationTimestamp);
		addAttribute(RefactoringPropertyAttributes.DESTINATION_METHOD_ID, destinationMethodID);
		//Added ENTITY_NAME attribute to improve performance/scalability by avoiding some spurious matches.
		addAttribute(RefactoringPropertyAttributes.ENTITY_NAME, entityName);
		//Added ENTITY_NAME_NODE_ID attribute since it should be added whenever there is an ENTITY_NAME attribute to
		//avoid erroneous corrections.
		addAttribute(RefactoringPropertyAttributes.ENTITY_NAME_NODE_ID, entityNameNodeID);
		addAttribute(RefactoringPropertyAttributes.MOVE_ID, moveID);
	}

}
