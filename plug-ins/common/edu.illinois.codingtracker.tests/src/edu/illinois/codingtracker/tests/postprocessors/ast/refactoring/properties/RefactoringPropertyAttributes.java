/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties;


/**
 * This interface contains all possible attributes that a refactoring property may have.
 * 
 * @author Stas Negara
 * 
 */
public interface RefactoringPropertyAttributes {

	public static final String VARIABLE_NAME= "variableName";

	public static final String OLD_VARIABLE_NAME= "oldVariableName";

	public static final String NEW_VARIABLE_NAME= "newVariableName";

	public static final String MOVED_NODE= "movedNode";

	public static final String MOVE_ID= "moveID";

	public static final String PARENT_ID= "parentID";

}
