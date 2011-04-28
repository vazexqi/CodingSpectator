package org.eclipse.jdt.internal.corext.refactoring.codingspectator;

import org.eclipse.jface.viewers.IStructuredSelection;

import org.eclipse.jface.text.ITextSelection;

import org.eclipse.ltk.core.refactoring.codingspectator.IClearable;
import org.eclipse.ltk.core.refactoring.codingspectator.Logger;

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

	private ITextSelection selectionInEditor;

	IStructuredSelection structuredSelection;

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

	public void clearData() {
		resetInstance();
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

}
