package org.eclipse.jdt.internal.corext.refactoring.codingspectator;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.jface.text.ITextSelection;

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

	private ITypeRoot selectedTypeRootInEditor;

	private ITextSelection selectionInEditor;

	private IStructuredSelection structuredSelection;

	private boolean invokedThroughStructuredSelection;

	private RefactoringGlobalStore() {

	}

	private static void resetInstance() {
		instance= new RefactoringGlobalStore();
		Logger.clearable= instance;
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
		if (doesSelectionInEditorExist() && doesStructuredSelectionExist()) {
			JavaPlugin.log(new AssertionError("Capturing both structured and textual selections for a refactoring is unexpected.")); //$NON-NLS-1$
		}
	}

	public void setSelectionInEditor(ITextSelection selection) {
		selectionInEditor= selection;
		assertOnlyOneKindOfSelectionExists();
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
		structuredSelection= selection;
		setInvokedThroughStructuredSelection();
		assertOnlyOneKindOfSelectionExists();
	}

	private void setInvokedThroughStructuredSelection() {
		invokedThroughStructuredSelection= true;
	}

	public boolean isInvokedThroughStructuredSelection() {
		return invokedThroughStructuredSelection;
	}

	public boolean doesStructuredSelectionExist() {
		return structuredSelection != null;
	}

	//FIXME: Use structuredSelection.getFirstElement()
	public List getStructuredSelectionList() {
		return structuredSelection.toList();
	}

	public IJavaElement getFirstSelectedJavaElement() {
		return (IJavaElement)getStructuredSelectionList().get(0);
	}

	public void setSelectedTypeRootInEditor(ITypeRoot selectedTypeRootInEditor) {
		this.selectedTypeRootInEditor= selectedTypeRootInEditor;
	}

	public ITypeRoot getSelectedTypeRootInEditor() {
		return selectedTypeRootInEditor;
	}

	public void setEditorSelectionInfo(ITypeRoot editorInputJavaElement, ITextSelection selection) {
		setSelectedTypeRootInEditor(editorInputJavaElement);
		setSelectionInEditor(selection);
	}

}
