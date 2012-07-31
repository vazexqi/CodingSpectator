/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.operations.ast.InferredRefactoringOperation.RefactoringKind;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties.AtomicRefactoringProperty;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties.RefactoringProperty;



/**
 * This class represents a composite refactoring property that acts as both a refactoring property
 * and a fragment of a refactoring that combines several atomic refactoring properties.
 * 
 * @author Stas Negara
 * 
 */
public abstract class InferredRefactoringFragment extends InferredRefactoring implements RefactoringProperty {

	private final Set<InferredRefactoring> refactorings= new HashSet<InferredRefactoring>();

	private boolean isActive= true;


	public InferredRefactoringFragment() {
	}

	@Override
	public RefactoringKind getKind() {
		//Should not be invoked on a refactoring fragment.
		return null;
	}

	@Override
	public Map<String, String> getArguments() {
		//Should not be invoked on a refactoring fragment.
		return null;
	}

	@Override
	public ASTOperation getLastRelatedOperation() {
		return getLastContributingOperation();
	}

	@Override
	public void setLastContributingOperation(ASTOperation lastContributingOperation) {
		super.setLastContributingOperation(lastContributingOperation);
		for (InferredRefactoring refactoring : refactorings) {
			refactoring.setLastContributingOperation(lastContributingOperation);
		}
	}

	@Override
	public long getActivationTimestamp() {
		long activationTimestamp= -1;
		//Find the latest activation timestamp.
		for (RefactoringProperty refactoringProperty : getAllProperties()) {
			if (refactoringProperty.getActivationTimestamp() > activationTimestamp) {
				activationTimestamp= refactoringProperty.getActivationTimestamp();
			}
		}
		return activationTimestamp;
	}

	@Override
	public void checkTimeout(long currentTimestamp) {
		if (currentTimestamp - getActivationTimestamp() >= decayTimeThreshold) {
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
		disableProperties(); //Disable component properties.
		isActive= false;
		InferredRefactoringFactory.disabledProperty(this);
		AtomicRefactoringProperty.notifyRefactoringsAboutDisabledProperty(refactorings, this);
	}

	@Override
	public void addRefactoring(InferredRefactoring refactoring) {
		refactorings.add(refactoring);
	}

	@Override
	public void removeRefactoring(InferredRefactoring refactoring) {
		refactorings.remove(refactoring);
	}

	@Override
	public Object getAttribute(String name) {
		for (RefactoringProperty refactoringProperty : getAllProperties()) {
			Object attribute= refactoringProperty.getAttribute(name);
			if (attribute != null) {
				return attribute;
			}
		}
		return null; //Should not reach here.
	}

	@Override
	public boolean doesMatch(InferredRefactoring containingRefactoring, RefactoringProperty anotherProperty) {
		for (RefactoringProperty refactoringProperty : getAllProperties()) {
			if (!refactoringProperty.doesMatch(containingRefactoring, anotherProperty)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void setMainOperation(ASTOperation mainOperation) {
		for (RefactoringProperty refactoringProperty : getAllProperties()) {
			refactoringProperty.setMainOperation(mainOperation);
		}
	}

	@Override
	public void fireCorrected() {
		throw new RuntimeException("A refactoring fragment should never be corrected!");
	}

}
