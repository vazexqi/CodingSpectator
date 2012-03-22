/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.illinois.codingtracker.operations.ast.InferredRefactoringOperation.RefactoringKind;
import edu.illinois.codingtracker.tests.postprocessors.ast.move.NodeDescriptor;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties.RefactoringProperties;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties.RefactoringProperty;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties.RefactoringPropertyAttributes;



/**
 * This class represents an instance of partially or fully inferred Extract Constant refactoring
 * (i.e., extracting a constant or an expression to a field).
 * 
 * @author Stas Negara
 * 
 */
public class ExtractConstantRefactoring extends InferredRefactoring {

	private static final Set<String> acceptableProperties= new HashSet<String>();

	static {
		acceptableProperties.add(RefactoringProperties.MOVED_TO_FIELD_INITIALIZATION);
		acceptableProperties.add(RefactoringProperties.ADDED_FIELD_DECLARATION);
		acceptableProperties.add(RefactoringFragments.REPLACED_EXPRESSION_WITH_ENTITY);
	}


	private ExtractConstantRefactoring() {

	}

	public static ExtractConstantRefactoring createRefactoring(RefactoringProperty refactoringProperty) {
		if (!isAcceptableProperty(refactoringProperty)) {
			throw new RuntimeException("Can not create ExtractConstantRefactoring for property: " + refactoringProperty);
		}
		ExtractConstantRefactoring newRefactoring= new ExtractConstantRefactoring();
		addProperty(newRefactoring, refactoringProperty);
		return newRefactoring;
	}

	public static boolean isAcceptableProperty(RefactoringProperty refactoringProperty) {
		return acceptableProperties.contains(refactoringProperty.getClassName());
	}

	@Override
	public boolean isMultiProperty(String propertyName) {
		return propertyName.equals(RefactoringFragments.REPLACED_EXPRESSION_WITH_ENTITY);
	}

	@Override
	protected InferredRefactoring createFreshInstance() {
		return new ExtractConstantRefactoring();
	}

	@Override
	protected Set<String> getAcceptableProperties() {
		return acceptableProperties;
	}

	@Override
	public RefactoringKind getKind() {
		return RefactoringKind.EXTRACT_CONSTANT;
	}

	@Override
	public Map<String, String> getArguments() {
		RefactoringProperty refactoringProperty= getProperty(RefactoringProperties.MOVED_TO_FIELD_INITIALIZATION);
		String entityName= (String)refactoringProperty.getAttribute(RefactoringPropertyAttributes.ENTITY_NAME);
		NodeDescriptor nodeDescriptor= (NodeDescriptor)refactoringProperty.getAttribute(RefactoringPropertyAttributes.MOVED_NODE);
		Map<String, String> arguments= new HashMap<String, String>();
		arguments.put("FieldName", entityName);
		arguments.put("ExtractedValue", nodeDescriptor.getNodeText());
		return arguments;
	}

}
