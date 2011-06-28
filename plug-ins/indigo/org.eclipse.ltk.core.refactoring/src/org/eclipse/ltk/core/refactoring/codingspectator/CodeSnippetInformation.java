package org.eclipse.ltk.core.refactoring.codingspectator;

import java.util.Map;

import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;

/**
 * Stores a triple of associated code snippet information for the selected text of the current
 * refactoring
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class CodeSnippetInformation {
	private String codeSnippet;

	private String relativeOffset;

	private String selectedText;

	public CodeSnippetInformation(String codeSnippet, String relativeOffset, String selectedText) {
		this(codeSnippet, selectedText);
		this.relativeOffset= relativeOffset;
	}

	public CodeSnippetInformation(String codeSnippet, String selectedText) {
		this(selectedText);
		this.codeSnippet= codeSnippet;
	}

	public CodeSnippetInformation(String selectedText) {
		this.selectedText= selectedText;
	}

	public CodeSnippetInformation() {
	}

	public void insertIntoMap(Map arguments) {
		addToArgumentsIfNonNull(arguments, RefactoringDescriptor.ATTRIBUTE_SELECTION_TEXT, selectedText);
		addToArgumentsIfNonNull(arguments, RefactoringDescriptor.ATTRIBUTE_CODE_SNIPPET, codeSnippet);
		addToArgumentsIfNonNull(arguments, RefactoringDescriptor.ATTRIBUTE_SELECTION_IN_CODE_SNIPPET, relativeOffset);
	}

	private void addToArgumentsIfNonNull(Map arguments, String key, String value) {
		if (value != null) {
			arguments.put(key, value);
		}
	}

	public String toString() {
		StringBuilder buffer= new StringBuilder();
		buffer.append(RefactoringDescriptor.ATTRIBUTE_CODE_SNIPPET);
		buffer.append("="); //$NON-NLS-1$
		buffer.append(codeSnippet);
		buffer.append(" "); //$NON-NLS-1$
		buffer.append(RefactoringDescriptor.ATTRIBUTE_SELECTION_IN_CODE_SNIPPET);
		buffer.append("="); //$NON-NLS-1$
		buffer.append(relativeOffset);
		buffer.append(" "); //$NON-NLS-1$
		buffer.append(RefactoringDescriptor.ATTRIBUTE_SELECTION_TEXT);
		buffer.append("="); //$NON-NLS-1$
		buffer.append(selectedText);
		return buffer.toString();
	}



}
