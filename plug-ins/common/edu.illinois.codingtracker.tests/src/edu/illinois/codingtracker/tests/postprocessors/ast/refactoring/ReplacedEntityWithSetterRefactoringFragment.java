/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring;

import java.util.HashSet;
import java.util.Set;

import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties.RefactoringProperties;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties.RefactoringProperty;



/**
 * 
 * @author Stas Negara
 * 
 */
public class ReplacedEntityWithSetterRefactoringFragment extends InferredRefactoringFragment {

	private static final Set<String> acceptableProperties= new HashSet<String>();

	static {
		acceptableProperties.add(RefactoringProperties.ADDED_SETTER_METHOD_INVOCATION);
		acceptableProperties.add(RefactoringProperties.DELETED_ENTITY_REFERENCE);
	}


	private ReplacedEntityWithSetterRefactoringFragment() {

	}

	public static ReplacedEntityWithSetterRefactoringFragment createRefactoring(RefactoringProperty refactoringProperty) {
		if (!isAcceptableProperty(refactoringProperty)) {
			throw new RuntimeException("Can not create ReplacedEntityWithSetterRefactoringFragment for property: " + refactoringProperty);
		}
		ReplacedEntityWithSetterRefactoringFragment newRefactoring= new ReplacedEntityWithSetterRefactoringFragment();
		addProperty(newRefactoring, refactoringProperty);
		return newRefactoring;
	}

	public static boolean isAcceptableProperty(RefactoringProperty refactoringProperty) {
		return !RefactoringInferencePostprocessor.isIntroducingGetterInvocation &&
				acceptableProperties.contains(refactoringProperty.getClassName());
	}

	@Override
	public boolean isMultiProperty(String propertyName) {
		return false;
	}

	@Override
	protected InferredRefactoring createFreshInstance() {
		return new ReplacedEntityWithSetterRefactoringFragment();
	}

	@Override
	protected Set<String> getAcceptableProperties() {
		return acceptableProperties;
	}

}
