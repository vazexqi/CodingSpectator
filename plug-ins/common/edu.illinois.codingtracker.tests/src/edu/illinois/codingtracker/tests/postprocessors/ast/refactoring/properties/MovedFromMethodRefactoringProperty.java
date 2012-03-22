/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties;



/**
 * This class represents a moved AST node that is deleted from some method.
 * 
 * @author Stas Negara
 * 
 */
public class MovedFromMethodRefactoringProperty extends AtomicRefactoringProperty {


	public MovedFromMethodRefactoringProperty(long sourceMethodID, long moveID, long activationTimestamp) {
		super(activationTimestamp);
		addAttribute(RefactoringPropertyAttributes.SOURCE_METHOD_ID, sourceMethodID);
		addAttribute(RefactoringPropertyAttributes.MOVE_ID, moveID);
	}

}
