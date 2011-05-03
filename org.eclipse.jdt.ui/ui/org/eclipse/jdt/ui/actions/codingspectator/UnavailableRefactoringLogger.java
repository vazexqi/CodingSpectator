package org.eclipse.jdt.ui.actions.codingspectator;

import org.eclipse.ltk.core.refactoring.codingspectator.CodeSnippetInformation;
import org.eclipse.ltk.core.refactoring.codingspectator.Logger;

import org.eclipse.jdt.core.ITypeRoot;

import org.eclipse.jdt.internal.corext.refactoring.codingspectator.CodeSnippetInformationFactory;

import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class UnavailableRefactoringLogger {

	//Record the invocation of the refactoring when it is not available.
	public static void logUnavailableRefactoringEvent(JavaEditor editor, String RefactoringID, String errorMessage) {
		ITypeRoot typeRoot= SelectionConverter.getInput(editor);
		if (typeRoot != null) {
			String javaProject= typeRoot.getJavaProject().getElementName();
			CodeSnippetInformation codeSnippetInformation= CodeSnippetInformationFactory.extractCodeSnippetInformation(typeRoot);
			Logger.logUnavailableRefactoringEvent(RefactoringID, javaProject, codeSnippetInformation, errorMessage);
		}
	}
}
