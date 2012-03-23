/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.illinois.codingtracker.operations.ast.InferredRefactoringOperation.RefactoringKind;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties.RefactoringProperties;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties.RefactoringProperty;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties.RefactoringPropertyAttributes;



/**
 * This class represents an instance of partially or fully inferred Extract Method refactoring.
 * 
 * @author Stas Negara
 * 
 */
public class ExtractMethodRefactoring extends InferredRefactoring {

	private static final Set<String> acceptableProperties= new HashSet<String>();

	static {
		acceptableProperties.add(RefactoringProperties.ADDED_METHOD_DECLARATION);
		acceptableProperties.add(RefactoringProperties.ADDED_METHOD_INVOCATION);
		acceptableProperties.add(RefactoringFragments.MOVED_ACROSS_METHODS);
	}


	private ExtractMethodRefactoring() {

	}

	public static ExtractMethodRefactoring createRefactoring(RefactoringProperty refactoringProperty) {
		if (!isAcceptableProperty(refactoringProperty)) {
			throw new RuntimeException("Can not create ExtractMethodRefactoring for property: " + refactoringProperty);
		}
		ExtractMethodRefactoring newRefactoring= new ExtractMethodRefactoring();
		addProperty(newRefactoring, refactoringProperty);
		return newRefactoring;
	}

	public static boolean isAcceptableProperty(RefactoringProperty refactoringProperty) {
		return acceptableProperties.contains(refactoringProperty.getClassName());
	}

	@Override
	public boolean isMultiProperty(String propertyName) {
		return propertyName.equals(RefactoringFragments.MOVED_ACROSS_METHODS);
	}

	@Override
	protected InferredRefactoring createFreshInstance() {
		return new ExtractMethodRefactoring();
	}

	@Override
	protected Set<String> getAcceptableProperties() {
		return acceptableProperties;
	}

	@Override
	public RefactoringKind getKind() {
		return RefactoringKind.EXTRACT_METHOD;
	}

	@Override
	public Map<String, String> getArguments() {
		RefactoringProperty refactoringProperty= getProperty(RefactoringProperties.ADDED_METHOD_INVOCATION);
		String sourceMethodName= (String)refactoringProperty.getAttribute(RefactoringPropertyAttributes.SOURCE_METHOD_NAME);
		String destinationMethodName= (String)refactoringProperty.getAttribute(RefactoringPropertyAttributes.ENTITY_NAME);
		Map<String, String> arguments= new HashMap<String, String>();
		arguments.put("SourceMethodName", sourceMethodName);
		arguments.put("DestinationMethodName", destinationMethodName);
		return arguments;
	}

}
