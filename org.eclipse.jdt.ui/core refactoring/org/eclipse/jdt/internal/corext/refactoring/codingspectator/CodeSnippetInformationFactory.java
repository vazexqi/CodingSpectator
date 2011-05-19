package org.eclipse.jdt.internal.corext.refactoring.codingspectator;

import org.eclipse.ltk.core.refactoring.codingspectator.CodeSnippetInformation;

import org.eclipse.jdt.core.ITypeRoot;

import org.eclipse.jdt.internal.ui.JavaPlugin;

/**
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class CodeSnippetInformationFactory {

	//FIXME: Remove typeroot parameter
	private static CodeSnippetInformationExtractor createCodeSnippetInformationExtractor(RefactoringGlobalStore store, ITypeRoot typeRoot) {
		if (typeRoot == null) {
			return new NullCodeSnippetInformationExtractor();
		}
		if (store.isInvokedThroughStructuredSelection()) {
			try {
				if (store.doesStructuredSelectionExist()) {
					return new StructuredSelectionCodeSnippetInformationExtractor(typeRoot, store.getFirstSelectedJavaElement(), store.getStructuredSelectionList().toString());
				} else {
					return new NullCodeSnippetInformationExtractor();
				}
			} catch (ClassCastException e) {
				JavaPlugin.log(e);
				return new NullCodeSnippetInformationExtractor();
			}
		} else {
			if (store.doesSelectionInEditorExist()) {
				return new TextSelectionCodeSnippetInformationExtractor(store.getSelectedTypeRootInEditor(), store.getSelectionStart(), store.getSelectionLength());
			} else {
				return new NullCodeSnippetInformationExtractor();
			}
		}
	}

	private static CodeSnippetInformation extractCodeSnippetInformation(RefactoringGlobalStore store, ITypeRoot typeRoot) {
		return createCodeSnippetInformationExtractor(store, typeRoot).extractCodeSnippetInformation();
	}

	public static CodeSnippetInformation extractCodeSnippetInformation(ITypeRoot typeRoot) {
		return extractCodeSnippetInformation(RefactoringGlobalStore.getInstance(), typeRoot);
	}

}
