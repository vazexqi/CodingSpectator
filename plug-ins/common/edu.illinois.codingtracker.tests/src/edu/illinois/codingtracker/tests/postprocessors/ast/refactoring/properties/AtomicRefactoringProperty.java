/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.EncapsulateFieldRefactoring;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.ExtractConstantRefactoring;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.ExtractMethodRefactoring;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.ExtractVariableRefactoring;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.InferredRefactoring;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.InferredRefactoringFactory;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.InlineVariableRefactoring;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.RenameClassRefactoring;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.RenameFieldRefactoring;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.RenameMethodRefactoring;



/**
 * 
 * @author Stas Negara
 * 
 */
public abstract class AtomicRefactoringProperty implements RefactoringProperty {

	//Attributes, whose values should be different to allow the properties to match.
	private static final Map<String, String> disjointAttributes= new HashMap<String, String>();

	static {
		//Disjoint attributes are (obviously) symmetrical.
		disjointAttributes.put(RefactoringPropertyAttributes.SOURCE_METHOD_ID, RefactoringPropertyAttributes.DESTINATION_METHOD_ID);
		disjointAttributes.put(RefactoringPropertyAttributes.DESTINATION_METHOD_ID, RefactoringPropertyAttributes.SOURCE_METHOD_ID);
		disjointAttributes.put(RefactoringPropertyAttributes.GETTER_METHOD_ID, RefactoringPropertyAttributes.SETTER_METHOD_ID);
		disjointAttributes.put(RefactoringPropertyAttributes.SETTER_METHOD_ID, RefactoringPropertyAttributes.GETTER_METHOD_ID);
	}

	private final Map<String, Object> attributes= new HashMap<String, Object>();

	private final Set<InferredRefactoring> refactorings= new HashSet<InferredRefactoring>();

	private boolean isActive= true;

	private long activationTimestamp;

	private ASTOperation causingOperation;


	public AtomicRefactoringProperty(long activationTimestamp) {
		this.activationTimestamp= activationTimestamp;
	}

	public void setCausingOperation(ASTOperation causingOperation) {
		this.causingOperation= causingOperation;
	}

	@Override
	public ASTOperation getCausingOperation() {
		return causingOperation;
	}

	@Override
	public long getActivationTimestamp() {
		return activationTimestamp;
	}

	protected void updateActivationTimestamp(long newActivationTimestamp) {
		activationTimestamp= newActivationTimestamp;
	}

	@Override
	public void checkTimeout(long currentTimestamp) {
		if (currentTimestamp - activationTimestamp >= decayTimeThreshold) {
			disable();
		}
	}

	@Override
	public String getClassName() {
		return getClass().getSimpleName();
	}

	@Override
	public boolean isActive() {
		return isActive;
	}

	@Override
	public void disable() {
		isActive= false;
		InferredRefactoringFactory.disabledProperty(this);
		for (InferredRefactoring refactoring : refactorings) {
			refactoring.disabledProperty(this);
		}
		refactorings.clear();
	}

	@Override
	public void addRefactoring(InferredRefactoring refactoring) {
		refactorings.add(refactoring);
	}

	@Override
	public void removeRefactoring(InferredRefactoring refactoring) {
		refactorings.remove(refactoring);
	}

	public void fireCorrected() {
		//Use a temporary collection since a corrected property might lead to a removed refactoring.
		Set<InferredRefactoring> existingRefactorings= new HashSet<InferredRefactoring>();
		existingRefactorings.addAll(refactorings);
		for (InferredRefactoring refactoring : existingRefactorings) {
			refactoring.correctedProperty(this);
		}
	}

	protected void addAttribute(String name, Object value) {
		attributes.put(name, value);
	}

	@Override
	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	@Override
	public boolean doesMatch(InferredRefactoring containingRefactoring, RefactoringProperty anotherProperty) {
		for (Entry<String, Object> entry : attributes.entrySet()) {
			String attribute= entry.getKey();
			if (!isIgnoredAttribute(attribute, containingRefactoring)) {
				Object objectToMatch= anotherProperty.getAttribute(attribute);
				if (objectToMatch != null && !objectToMatch.equals(entry.getValue())) {
					return false;
				}
				Object objectToDisjoin= anotherProperty.getAttribute(disjointAttributes.get(attribute));
				if (objectToDisjoin != null && objectToDisjoin.equals(entry.getValue())) {
					return false;
				}
			}
		}
		return true;
	}

	protected boolean isIgnoredAttribute(String attribute, InferredRefactoring containingRefactoring) {
		if ((containingRefactoring instanceof ExtractConstantRefactoring ||
				containingRefactoring instanceof ExtractVariableRefactoring ||
				containingRefactoring instanceof InlineVariableRefactoring ||
				containingRefactoring instanceof EncapsulateFieldRefactoring) &&
				attribute.equals(RefactoringPropertyAttributes.PARENT_ID)) {
			return true;
		}
		if ((containingRefactoring instanceof RenameClassRefactoring ||
				containingRefactoring instanceof RenameFieldRefactoring ||
				containingRefactoring instanceof RenameMethodRefactoring) &&
				attribute.equals(RefactoringPropertyAttributes.SOURCE_METHOD_NAME)) {
			return true;
		}
		if (containingRefactoring instanceof ExtractMethodRefactoring &&
				attribute.equals(RefactoringPropertyAttributes.MOVE_ID)) {
			return true;
		}
		return attribute.equals(RefactoringPropertyAttributes.ENTITY_NAME_NODE_ID);
	}

	protected static boolean isVeryCloseButDistinct(AtomicRefactoringProperty property1, AtomicRefactoringProperty property2) {
		return property1.activationTimestamp != property2.activationTimestamp &&
				Math.abs(property1.activationTimestamp - property2.activationTimestamp) < closenessTimeThreshold;
	}

}
