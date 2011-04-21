package org.eclipse.jdt.internal.corext.refactoring.codingspectator;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.jface.text.ITextSelection;

import org.eclipse.ltk.core.refactoring.codingspectator.CodeSnippetInformation;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ITypeRoot;

import org.eclipse.jdt.internal.ui.JavaPlugin;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class RefactoringGlobalStore {
	private static RefactoringGlobalStore instance= new RefactoringGlobalStore();

	private ITextSelection selectionInEditor;

	private IStructuredSelection structuredSelection;

	private boolean invokedThroughStructuredSelection;

	private RefactoringGlobalStore() {

	}

	private RefactoringGlobalStore(ITextSelection selectionInEditor, IStructuredSelection structuredSelection, boolean invokedThroughStructuredSelection) {
		this.selectionInEditor= selectionInEditor;
		this.structuredSelection= structuredSelection;
		this.invokedThroughStructuredSelection= invokedThroughStructuredSelection;
	}

	public RefactoringGlobalStore getShallowCopy() {
		return new RefactoringGlobalStore(selectionInEditor, structuredSelection, invokedThroughStructuredSelection);
	}

	public static RefactoringGlobalStore getNewInstance() {
		clearData();
		return getInstance();
	}

	public static RefactoringGlobalStore getInstance() {
		return instance;
	}

	public void setSelectionInEditor(ITextSelection selection) {
		selectionInEditor= selection;
	}

	public int getSelectionStart() {
		return selectionInEditor.getOffset();
	}

	public int getSelectionLength() {
		return selectionInEditor.getLength();
	}

	public boolean hasData() {
		return selectionInEditor != null;
	}

	public static void clearData() {
		instance= new RefactoringGlobalStore();
	}

	public void setStructuredSelection(IStructuredSelection selection) {
		structuredSelection= selection;
		setInvokedThroughStructuredSelection();
	}

	private void setInvokedThroughStructuredSelection() {
		invokedThroughStructuredSelection= true;
	}

	public boolean isInvokedThroughStructuredSelection() {
		return invokedThroughStructuredSelection;
	}

	private class CodeSnippetExtractorFactory {
		public CodeSnippetInformationExtractor createCodeSnippetInformationExtractor(ITypeRoot typeRoot) {
			if (isInvokedThroughStructuredSelection()) {
				try {
					List selectionList= structuredSelection.toList();
					IJavaElement aSelectedElement= (IJavaElement)selectionList.get(0);
					return new StructuredSelectionCodeSnippetInformationExtractor(typeRoot, aSelectedElement, selectionList.toString());
				} catch (ClassCastException e) {
					JavaPlugin.log(e);
					return new NullCodeSnippetInformationExtractor();
				}
			} else {
				return new TextSelectionCodeSnippetInformationExtractor(typeRoot, RefactoringGlobalStore.this.getSelectionStart(), RefactoringGlobalStore.this.getSelectionLength());
			}
		}
	}

	public CodeSnippetInformation extractCodeSnippetInformation(ITypeRoot typeRoot) {
		return new CodeSnippetExtractorFactory().createCodeSnippetInformationExtractor(typeRoot).extractCodeSnippetInformation();
	}

}
