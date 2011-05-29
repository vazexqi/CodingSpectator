package org.eclipse.jdt.internal.corext.refactoring.codingspectator;

import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.jface.text.ITextSelection;

import org.eclipse.ltk.core.refactoring.codingspectator.CodeSnippetInformation;
import org.eclipse.ltk.core.refactoring.codingspectator.IClearable;
import org.eclipse.ltk.core.refactoring.codingspectator.Logger;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ITypeRoot;

import org.eclipse.jdt.internal.ui.JavaPlugin;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class RefactoringGlobalStore implements IClearable {
	private static RefactoringGlobalStore instance;

	static {
		resetInstance();
	}

	private ITypeRoot selectedTypeRoot;

	private ITextSelection selectionInEditor;

	private IJavaElement selectedElement;

	private CodeSnippetInformation codeSnippetInformation;

	private String selectedElementsText;

	private boolean invokedThroughStructuredSelection;

	private RefactoringGlobalStore() {

	}

	private static void resetInstance() {
		instance= new RefactoringGlobalStore();
		Logger.clearable= instance;
	}

	public static RefactoringGlobalStore getNewInstance() {
		resetInstance();
		return getInstance();
	}

	public static RefactoringGlobalStore getInstance() {
		return instance;
	}

	public void clearData() {
		resetInstance();
	}

	/**
	 * This method specifies an object invariant.
	 */
	private void assertOnlyOneKindOfSelectionExists() {
		if (doesSelectionInEditorExist() && isInvokedThroughStructuredSelection()) {
			JavaPlugin.log(new AssertionError("Capturing both structured and textual selections for a refactoring is unexpected.")); //$NON-NLS-1$
		}
	}

	private void setSelectionInEditor(ITextSelection selection) {
		selectionInEditor= selection;
	}

	public int getSelectionStart() {
		return selectionInEditor.getOffset();
	}

	public int getSelectionLength() {
		return selectionInEditor.getLength();
	}

	public boolean doesSelectionInEditorExist() {
		return selectionInEditor != null;
	}

	public void setStructuredSelection(IStructuredSelection selection) {
		Object firstSelectedElement= selection.getFirstElement();
		if (firstSelectedElement instanceof IJavaElement) {
			selectedElement= (IJavaElement)firstSelectedElement;
			selectedTypeRoot= (ITypeRoot)selectedElement.getAncestor(IJavaElement.COMPILATION_UNIT);
		}
		selectedElementsText= selection.toString();
		setInvokedThroughStructuredSelection();
		codeSnippetInformation= CodeSnippetInformationFactory.extractCodeSnippetInformation();
		assertOnlyOneKindOfSelectionExists();
	}

	private void setInvokedThroughStructuredSelection() {
		invokedThroughStructuredSelection= true;
	}

	public boolean isInvokedThroughStructuredSelection() {
		return invokedThroughStructuredSelection;
	}

	public IJavaElement getSelectedJavaElement() {
		return selectedElement;
	}

	private void setSelectedTypeRoot(ITypeRoot selectedTypeRootInEditor) {
		this.selectedTypeRoot= selectedTypeRootInEditor;
	}

	public ITypeRoot getSelectedTypeRoot() {
		return selectedTypeRoot;
	}

	public void setEditorSelectionInfo(ITypeRoot editorInputJavaElement, ITextSelection selection) {
		setSelectedTypeRoot(editorInputJavaElement);
		setSelectionInEditor(selection);
		codeSnippetInformation= CodeSnippetInformationFactory.extractCodeSnippetInformation();
		assertOnlyOneKindOfSelectionExists();
	}

	public String getSelectedElementsText() {
		return selectedElementsText;
	}

	public CodeSnippetInformation getCodeSnippetInformation() {
		return codeSnippetInformation;
	}

}
