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

	public static final String ENTITY_NAME= "entityName";

	public static final String OLD_ENTITY_NAME= "oldEntityName";

	public static final String NEW_ENTITY_NAME= "newEntityName";

	public static final String MOVED_NODE= "movedNode";

	public static final String MOVE_ID= "moveID";

	public static final String PARENT_ID= "parentID";

}
