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
 * This class represents an instance of partially or fully inferred Encapsulate Field refactoring.
 * 
 * @author Stas Negara
 * 
 */
public class EncapsulateFieldRefactoring extends InferredRefactoring {

	private static final Set<String> acceptableProperties= new HashSet<String>();

	static {
		acceptableProperties.add(RefactoringProperties.ADDED_GETTER_METHOD_DECLARATION);
		acceptableProperties.add(RefactoringProperties.ADDED_SETTER_METHOD_DECLARATION);
		acceptableProperties.add(RefactoringProperties.ADDED_FIELD_RETURN);
		acceptableProperties.add(RefactoringProperties.ADDED_FIELD_ASSIGNMENT);
		acceptableProperties.add(RefactoringProperties.MADE_FIELD_PRIVATE);
		acceptableProperties.add(RefactoringFragments.REPLACED_ENTITY_WITH_GETTER);
		acceptableProperties.add(RefactoringFragments.REPLACED_ENTITY_WITH_SETTER);
	}


	private EncapsulateFieldRefactoring() {

	}

	public static EncapsulateFieldRefactoring createRefactoring(RefactoringProperty refactoringProperty) {
		if (!isAcceptableProperty(refactoringProperty)) {
			throw new RuntimeException("Can not create EncapsulateFieldRefactoring for property: " + refactoringProperty);
		}
		EncapsulateFieldRefactoring newRefactoring= new EncapsulateFieldRefactoring();
		addProperty(newRefactoring, refactoringProperty);
		return newRefactoring;
	}

	public static boolean isAcceptableProperty(RefactoringProperty refactoringProperty) {
		return acceptableProperties.contains(refactoringProperty.getClassName());
	}

	@Override
	public boolean isMultiProperty(String propertyName) {
		return propertyName.equals(RefactoringFragments.REPLACED_ENTITY_WITH_GETTER) ||
				propertyName.equals(RefactoringFragments.REPLACED_ENTITY_WITH_SETTER);
	}

	@Override
	protected boolean isOptionalProperty(String propertyName) {
		return propertyName.equals(RefactoringFragments.REPLACED_ENTITY_WITH_GETTER) ||
				propertyName.equals(RefactoringFragments.REPLACED_ENTITY_WITH_SETTER);
	}

	@Override
	protected InferredRefactoring createFreshInstance() {
		return new EncapsulateFieldRefactoring();
	}

	@Override
	protected Set<String> getAcceptableProperties() {
		return acceptableProperties;
	}

	@Override
	public RefactoringKind getKind() {
		return RefactoringKind.ENCAPSULATE_FIELD;
	}

	@Override
	public Map<String, String> getArguments() {
		RefactoringProperty refactoringProperty= getProperty(RefactoringProperties.ADDED_GETTER_METHOD_DECLARATION);
		String getterMethodName= (String)refactoringProperty.getAttribute(RefactoringPropertyAttributes.GETTER_METHOD_NAME);
		refactoringProperty= getProperty(RefactoringProperties.ADDED_SETTER_METHOD_DECLARATION);
		String setterMethodName= (String)refactoringProperty.getAttribute(RefactoringPropertyAttributes.SETTER_METHOD_NAME);
		refactoringProperty= getProperty(RefactoringProperties.ADDED_FIELD_RETURN);
		String entityName= (String)refactoringProperty.getAttribute(RefactoringPropertyAttributes.ENTITY_NAME);
		Map<String, String> arguments= new HashMap<String, String>();
		arguments.put("EncapsulatedFieldName", entityName);
		arguments.put("GetterMethodName", getterMethodName);
		arguments.put("SetterMethodName", setterMethodName);
		return arguments;
	}

}
