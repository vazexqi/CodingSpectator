package org.eclipse.jdt.internal.corext.refactoring.codingspectator;

import org.eclipse.ltk.core.refactoring.codingspectator.CodeSnippetInformation;

import org.eclipse.jdt.internal.ui.JavaPlugin;

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
			try {
				if (store.isInvokedThroughStructuredSelection()) {
					return new StructuredSelectionCodeSnippetInformationExtractor(store.getSelectedTypeRoot(), store.getSelectedJavaElement(), store.getSelectedElementsText());
				} else {
					return new NullCodeSnippetInformationExtractor();
				}
			} catch (ClassCastException e) {
				JavaPlugin.log(e);
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
		return extractCodeSnippetInformation(RefactoringGlobalStore.getInstance());
	}

}
