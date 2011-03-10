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
		this.codeSnippet= codeSnippet;
		this.relativeOffset= relativeOffset;
		this.selectedText= selectedText;
	}

	public void insertIntoMap(Map arguments) {
		arguments.put(RefactoringDescriptor.ATTRIBUTE_SELECTION_TEXT, selectedText);
		arguments.put(RefactoringDescriptor.ATTRIBUTE_CODE_SNIPPET, codeSnippet);
		arguments.put(RefactoringDescriptor.ATTRIBUTE_SELECTION_IN_CODE_SNIPPET, relativeOffset);
	}
}
