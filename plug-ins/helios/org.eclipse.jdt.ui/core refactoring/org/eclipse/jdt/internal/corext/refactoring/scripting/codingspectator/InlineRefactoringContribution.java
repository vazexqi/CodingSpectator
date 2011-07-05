package org.eclipse.jdt.internal.corext.refactoring.scripting.codingspectator;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;

import org.eclipse.jdt.internal.core.refactoring.descriptors.RefactoringSignatureDescriptorFactory;
import org.eclipse.jdt.internal.corext.refactoring.scripting.JavaUIRefactoringContribution;

/**
 * 
 * Refactoring contribution for the unknown inline refactoring. This class is based on
 * InlineConstantRefactoringContribution.
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public final class InlineRefactoringContribution extends JavaUIRefactoringContribution {

	/**
	 * 
	 * This refactoring descriptor does not correspond to a specific refactoring. Therefore, this
	 * factory method just returns null.
	 * 
	 */
	public final Refactoring createRefactoring(JavaRefactoringDescriptor descriptor, RefactoringStatus status) throws CoreException {
		return null;
	}

	public RefactoringDescriptor createDescriptor() {
		return RefactoringSignatureDescriptorFactory.createInlineDescriptor();
	}

	public RefactoringDescriptor createDescriptor(String id, String project, String description, String comment, Map arguments, int flags) {
		return RefactoringSignatureDescriptorFactory.createInlineDescriptor(project, description, comment, arguments, flags);
	}
}
