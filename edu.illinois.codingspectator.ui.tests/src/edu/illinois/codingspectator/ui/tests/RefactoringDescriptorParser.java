/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests;

import java.util.Map;

import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

/**
 * 
 * This class provides provides easy access to the contents of a refactoring descriptor.
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class RefactoringDescriptorParser {

	private JavaRefactoringDescriptor descriptor;

	public RefactoringDescriptorParser(JavaRefactoringDescriptor descriptor) {
		this.descriptor= descriptor;
	}

	@SuppressWarnings("rawtypes")
	private Map getArguments() {
		return descriptor.getArguments();
	}

	public String getSelection() {
		return (String)getArguments().get(RefactoringDescriptor.ATTRIBUTE_SELECTION);
	}

	public String getSelectionOffset() {
		return (String)getArguments().get(RefactoringDescriptor.ATTRIBUTE_SELECTION_OFFSET);
	}

	public String getStatus() {
		return (String)getArguments().get(RefactoringDescriptor.ATTRIBUTE_STATUS);
	}

	public String getCodeSnippet() {
		return (String)getArguments().get(RefactoringDescriptor.ATTRIBUTE_CODE_SNIPPET);
	}

}
