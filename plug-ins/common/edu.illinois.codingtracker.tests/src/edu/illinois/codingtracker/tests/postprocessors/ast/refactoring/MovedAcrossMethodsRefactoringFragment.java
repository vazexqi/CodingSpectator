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
public class MovedAcrossMethodsRefactoringFragment extends InferredRefactoringFragment {

	private static final Set<String> acceptableProperties= new HashSet<String>();

	static {
		acceptableProperties.add(RefactoringProperties.MOVED_FROM_METHOD);
		acceptableProperties.add(RefactoringProperties.MOVED_TO_METHOD);
	}


	private MovedAcrossMethodsRefactoringFragment() {

	}

	public static MovedAcrossMethodsRefactoringFragment createRefactoring(RefactoringProperty refactoringProperty) {
		if (!isAcceptableProperty(refactoringProperty)) {
			throw new RuntimeException("Can not create MovedAcrossMethodsRefactoringFragment for property: " + refactoringProperty);
		}
		MovedAcrossMethodsRefactoringFragment newRefactoring= new MovedAcrossMethodsRefactoringFragment();
		addProperty(newRefactoring, refactoringProperty);
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
		return new MovedAcrossMethodsRefactoringFragment();
	}

	@Override
	protected Set<String> getAcceptableProperties() {
		return acceptableProperties;
	}

}
