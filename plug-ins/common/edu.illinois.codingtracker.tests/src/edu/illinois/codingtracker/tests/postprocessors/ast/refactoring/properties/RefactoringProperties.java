/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties;


/**
 * This interface contains class names for all non-abstract classes derived from
 * AtomicRefactoringProperty.
 * 
 * @author Stas Negara
 * 
 */
public interface RefactoringProperties {

	public static final String ADDED_ENTITY_REFERENCE= "AddedEntityReferenceRefactoringProperty";

	public static final String ADDED_FIELD_ASSIGNMENT= "AddedFieldAssignmentRefactoringProperty";

	public static final String ADDED_FIELD_DECLARATION= "AddedFieldDeclarationRefactoringProperty";

	public static final String ADDED_FIELD_RETURN= "AddedFieldReturnRefactoringProperty";

	public static final String ADDED_GETTER_METHOD_DECLARATION= "AddedGetterMethodDeclarationRefactoringProperty";

	public static final String ADDED_METHOD_DECLARATION= "AddedMethodDeclarationRefactoringProperty";

	public static final String ADDED_METHOD_INVOCATION= "AddedMethodInvocationRefactoringProperty";

	public static final String ADDED_GETTER_METHOD_INVOCATION= "AddedGetterMethodInvocationRefactoringProperty";

	public static final String ADDED_SETTER_METHOD_INVOCATION= "AddedSetterMethodInvocationRefactoringProperty";

	public static final String ADDED_SETTER_METHOD_DECLARATION= "AddedSetterMethodDeclarationRefactoringProperty";

	public static final String ADDED_VARIABLE_DECLARATION= "AddedVariableDeclarationRefactoringProperty";

	public static final String CHANGED_FIELD_NAME_IN_DECLARATION= "ChangedFieldNameInDeclarationRefactoringProperty";

	public static final String CHANGED_METHOD_NAME_IN_DECLARATION= "ChangedMethodNameInDeclarationRefactoringProperty";

	public static final String CHANGED_TYPE_NAME_IN_DECLARATION= "ChangedTypeNameInDeclarationRefactoringProperty";

	public static final String CHANGED_TYPE_NAME_IN_CONSTRUCTOR= "ChangedTypeNameInConstructorRefactoringProperty";

	public static final String CHANGED_VARIABLE_NAME_IN_DECLARATION= "ChangedVariableNameInDeclarationRefactoringProperty";

	public static final String CHANGED_METHOD_NAME_IN_INVOCATION= "ChangedMethodNameInInvocationRefactoringProperty";

	public static final String CHANGED_LOCAL_ENTITY_NAME_IN_USAGE= "ChangedLocalEntityNameInUsageRefactoringProperty";

	public static final String CHANGED_GLOBAL_ENTITY_NAME_IN_USAGE= "ChangedGlobalEntityNameInUsageRefactoringProperty";

	public static final String DELETED_VARIABLE_DECLARATION= "DeletedVariableDeclarationRefactoringProperty";

	public static final String DELETED_ENTITY_REFERENCE= "DeletedEntityReferenceRefactoringProperty";

	public static final String MADE_FIELD_PRIVATE= "MadeFieldPrivateRefactoringProperty";

	public static final String MOVED_FROM_METHOD= "MovedFromMethodRefactoringProperty";

	public static final String MOVED_FROM_USAGE= "MovedFromUsageRefactoringProperty";

	public static final String MOVED_FROM_VARIABLE_INITIALIZATION= "MovedFromVariableInitializationRefactoringProperty";

	public static final String MOVED_TO_FIELD_INITIALIZATION= "MovedToFieldInitializationRefactoringProperty";

	public static final String MOVED_TO_METHOD= "MovedToMethodRefactoringProperty";

	public static final String MOVED_TO_USAGE= "MovedToUsageRefactoringProperty";

	public static final String MOVED_TO_VARIABLE_INITIALIZATION= "MovedToVariableInitializationRefactoringProperty";

}
