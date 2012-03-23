/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring;


/**
 * This interface contains class names for all classes derived from InferredRefactoringFragment.
 * 
 * @author Stas Negara
 * 
 */
public interface RefactoringFragments {

	public static final String REPLACED_EXPRESSION_WITH_ENTITY= "ReplacedExpressionWithEntityRefactoringFragment";

	public static final String REPLACED_ENTITY_WITH_EXPRESSION= "ReplacedEntityWithExpressionRefactoringFragment";

	public static final String REPLACED_ENTITY_WITH_GETTER= "ReplacedEntityWithGetterRefactoringFragment";

	public static final String REPLACED_ENTITY_WITH_SETTER= "ReplacedEntityWithSetterRefactoringFragment";

	public static final String MOVED_ACROSS_METHODS= "MovedAcrossMethodsRefactoringFragment";

}
