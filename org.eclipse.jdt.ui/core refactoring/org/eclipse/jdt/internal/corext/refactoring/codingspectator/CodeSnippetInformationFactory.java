package org.eclipse.jdt.internal.corext.refactoring.codingspectator;

import java.util.List;

import org.eclipse.ltk.core.refactoring.codingspectator.CodeSnippetInformation;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ITypeRoot;

import org.eclipse.jdt.internal.ui.JavaPlugin;

/**
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class CodeSnippetInformationFactory {

	private static CodeSnippetInformationExtractor createCodeSnippetInformationExtractor(RefactoringGlobalStore store, ITypeRoot typeRoot) {
		if (store.isInvokedThroughStructuredSelection()) {
			try {
				List selectionList= store.structuredSelection.toList();
				IJavaElement aSelectedElement= (IJavaElement)selectionList.get(0);
				return new StructuredSelectionCodeSnippetInformationExtractor(typeRoot, aSelectedElement, selectionList.toString());
			} catch (ClassCastException e) {
				JavaPlugin.log(e);
				return new NullCodeSnippetInformationExtractor();
			}
		} else {
			return new TextSelectionCodeSnippetInformationExtractor(typeRoot, store.getSelectionStart(), store.getSelectionLength());
		}
	}

	private static CodeSnippetInformation extractCodeSnippetInformation(RefactoringGlobalStore store, ITypeRoot typeRoot) {
		return createCodeSnippetInformationExtractor(store, typeRoot).extractCodeSnippetInformation();
	}

	public static CodeSnippetInformation extractCodeSnippetInformation(ITypeRoot typeRoot) {
		return extractCodeSnippetInformation(RefactoringGlobalStore.getInstance(), typeRoot);
	}

}
