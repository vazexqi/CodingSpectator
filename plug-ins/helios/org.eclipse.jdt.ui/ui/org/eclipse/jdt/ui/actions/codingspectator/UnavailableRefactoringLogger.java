package org.eclipse.jdt.ui.actions.codingspectator;

import org.eclipse.ltk.core.refactoring.codingspectator.CodeSnippetInformation;
import org.eclipse.ltk.core.refactoring.codingspectator.Logger;

import org.eclipse.jdt.internal.corext.refactoring.codingspectator.RefactoringGlobalStore;


/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class UnavailableRefactoringLogger {

	//Record the invocation of the refactoring when it is not available.
	public static void logUnavailableRefactoringEvent(String refactoringID, String errorMessage) {
		RefactoringGlobalStore instance= RefactoringGlobalStore.getInstance();
		String javaProject= instance.getProjectName();
		CodeSnippetInformation codeSnippetInformation= instance.getCodeSnippetInformation();
		Logger.logUnavailableRefactoringEvent(refactoringID, javaProject, codeSnippetInformation, errorMessage);
	}
}
