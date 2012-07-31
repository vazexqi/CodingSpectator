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
public class ReplacedEntityWithExpressionRefactoringFragment extends InferredRefactoringFragment {

	private static final Set<String> acceptableProperties= new HashSet<String>();

	static {
		acceptableProperties.add(RefactoringProperties.MOVED_TO_USAGE);
		acceptableProperties.add(RefactoringProperties.DELETED_ENTITY_REFERENCE);
	}


	private ReplacedEntityWithExpressionRefactoringFragment() {

	}

	public static ReplacedEntityWithExpressionRefactoringFragment createRefactoring(RefactoringProperty refactoringProperty) {
		if (!isAcceptableProperty(refactoringProperty)) {
			throw new RuntimeException("Can not create ReplacedEntityWithExpressionRefactoringFragment for property: " + refactoringProperty);
		}
		ReplacedEntityWithExpressionRefactoringFragment newRefactoring= new ReplacedEntityWithExpressionRefactoringFragment();
		addProperty(newRefactoring, refactoringProperty);
		return newRefactoring;
	}

	public static ReplacedEntityWithExpressionRefactoringFragment createRefactoring(RefactoringProperty refactoringProperty1, RefactoringProperty refactoringProperty2) {
		ReplacedEntityWithExpressionRefactoringFragment newRefactoring= createRefactoring(refactoringProperty1);
		if (!newRefactoring.canBePart(refactoringProperty2)) {
			throw new RuntimeException("Can not create a fragment from two unrelated properties!");
		}
		addProperty(newRefactoring, refactoringProperty2);
		return newRefactoring;
	}

	public static boolean isAcceptableProperty(RefactoringProperty refactoringProperty) {
		return acceptableProperties.contains(refactoringProperty.getClassName());
	}

	@Override
	public boolean isMultiProperty(String propertyName) {
		return false;
	}

	@Override
	protected InferredRefactoring createFreshInstance() {
		return new ReplacedEntityWithExpressionRefactoringFragment();
	}

	@Override
	protected Set<String> getAcceptableProperties() {
		return acceptableProperties;
	}

}
