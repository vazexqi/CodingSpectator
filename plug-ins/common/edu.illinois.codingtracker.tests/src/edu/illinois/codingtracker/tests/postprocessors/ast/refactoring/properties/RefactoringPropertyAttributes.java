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

	public static final String ENTITY_NAME_NODE_ID= "entityNameNodeID";

	public static final String OLD_ENTITY_NAME= "oldEntityName";

	public static final String NEW_ENTITY_NAME= "newEntityName";

	public static final String MOVED_NODE= "movedNode";

	public static final String MOVE_ID= "moveID";

	public static final String PARENT_ID= "parentID";

	public static final String ENCLOSING_CLASS_NODE_ID= "enclosingClassNodeID";

	public static final String DESTINATION_METHOD_ID= "destinationMethodID";

	public static final String SOURCE_METHOD_NAME= "sourceMethodName";

	public static final String SOURCE_METHOD_ID= "sourceMethodID";

	public static final String GETTER_METHOD_NAME= "getterMethodName";

	public static final String GETTER_METHOD_ID= "getterMethodID";

	public static final String SETTER_METHOD_NAME= "setterMethodName";

	public static final String SETTER_METHOD_ID= "setterMethodID";

}
