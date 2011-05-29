package org.eclipse.jdt.internal.corext.refactoring.codingspectator;

import org.eclipse.ltk.core.refactoring.codingspectator.CodeSnippetInformation;

/**
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class CodeSnippetInformationFactory {

	private static CodeSnippetInformationExtractor createCodeSnippetInformationExtractor(RefactoringGlobalStore store) {
		if (store.getSelectedTypeRoot() == null) {
			return new NullCodeSnippetInformationExtractor();
		}
		if (store.isInvokedThroughStructuredSelection()) {
			if (store.isInvokedThroughStructuredSelection()) {
				return new StructuredSelectionCodeSnippetInformationExtractor(store.getSelectedTypeRoot(), store.getSelectedJavaElement(), store.getSelectedElementsText());
			} else {
				return new NullCodeSnippetInformationExtractor();
			}
		} else {
			if (store.doesSelectionInEditorExist()) {
				return new TextSelectionCodeSnippetInformationExtractor(store.getSelectedTypeRoot(), store.getSelectionStart(), store.getSelectionLength());
			} else {
				return new NullCodeSnippetInformationExtractor();
			}
		}
	}

	private static CodeSnippetInformation extractCodeSnippetInformation(RefactoringGlobalStore store) {
		return createCodeSnippetInformationExtractor(store).extractCodeSnippetInformation();
	}

	public static CodeSnippetInformation extractCodeSnippetInformation() {
		RefactoringGlobalStore instance= RefactoringGlobalStore.getInstance();
		CodeSnippetInformation codeSnippetInformation= instance.getCodeSnippetInformation();
		if (codeSnippetInformation == null) {
			codeSnippetInformation= extractCodeSnippetInformation(instance);
		}
		return codeSnippetInformation;
	}

}
