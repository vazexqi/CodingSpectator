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
public class MovedToMethodRefactoringProperty extends RefactoringProperty {


	public MovedToMethodRefactoringProperty(long destinationMethodID, long moveID) {
		addAttribute(RefactoringPropertyAttributes.DESTINATION_METHOD_ID, destinationMethodID);
		addAttribute(RefactoringPropertyAttributes.MOVE_ID, moveID);
	}

}
