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
 * This class represents an instance of partially or fully inferred Rename Class refactoring.
 * 
 * @author Stas Negara
 * 
 */
public class RenameClassRefactoring extends InferredRefactoring {

	private static final Set<String> acceptableProperties= new HashSet<String>();

	static {
		acceptableProperties.add(RefactoringProperties.CHANGED_TYPE_NAME_IN_DECLARATION);
		acceptableProperties.add(RefactoringProperties.CHANGED_GLOBAL_ENTITY_NAME_IN_USAGE);
		acceptableProperties.add(RefactoringProperties.CHANGED_TYPE_NAME_IN_CONSTRUCTOR);
	}


	private RenameClassRefactoring() {

	}

	public static RenameClassRefactoring createRefactoring(RefactoringProperty refactoringProperty) {
		if (!isAcceptableProperty(refactoringProperty)) {
			throw new RuntimeException("Can not create RenameClassRefactoring for property: " + refactoringProperty);
		}
		RenameClassRefactoring newRefactoring= new RenameClassRefactoring();
		addProperty(newRefactoring, refactoringProperty);
		return newRefactoring;
	}

	public static boolean isAcceptableProperty(RefactoringProperty refactoringProperty) {
		return acceptableProperties.contains(refactoringProperty.getClassName());
	}

	@Override
	public boolean isComplete() {
		//Changing a type's name in the declaration and either in a reference or in a constructor is sufficient.
		return getPropertiesList(RefactoringProperties.CHANGED_TYPE_NAME_IN_DECLARATION) != null &&
				(getPropertiesList(RefactoringProperties.CHANGED_GLOBAL_ENTITY_NAME_IN_USAGE) != null ||
				getPropertiesList(RefactoringProperties.CHANGED_TYPE_NAME_IN_CONSTRUCTOR) != null);
	}

	@Override
	public boolean isMultiProperty(String propertyName) {
		return propertyName.equals(RefactoringProperties.CHANGED_GLOBAL_ENTITY_NAME_IN_USAGE) ||
				propertyName.equals(RefactoringProperties.CHANGED_TYPE_NAME_IN_CONSTRUCTOR);
	}

	@Override
	protected InferredRefactoring createFreshInstance() {
		return new RenameClassRefactoring();
	}

	@Override
	protected Set<String> getAcceptableProperties() {
		return acceptableProperties;
	}

	@Override
	public RefactoringKind getKind() {
		return RefactoringKind.RENAME_CLASS;
	}

	@Override
	public Map<String, String> getArguments() {
		RefactoringProperty refactoringProperty= getProperty(RefactoringProperties.CHANGED_TYPE_NAME_IN_DECLARATION);
		String oldEntityName= (String)refactoringProperty.getAttribute(RefactoringPropertyAttributes.OLD_ENTITY_NAME);
		String newEntityName= (String)refactoringProperty.getAttribute(RefactoringPropertyAttributes.NEW_ENTITY_NAME);
		Map<String, String> arguments= new HashMap<String, String>();
		arguments.put("OldName", oldEntityName);
		arguments.put("NewName", newEntityName);
		return arguments;
	}

}
