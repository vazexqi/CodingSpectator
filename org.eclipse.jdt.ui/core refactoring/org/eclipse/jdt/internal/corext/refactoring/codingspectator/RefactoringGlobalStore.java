package org.eclipse.jdt.internal.corext.refactoring.codingspectator;

import org.eclipse.jface.text.ITextSelection;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class RefactoringGlobalStore {
	final static RefactoringGlobalStore instance= new RefactoringGlobalStore();

	ITextSelection selectionInEditor;

	private RefactoringGlobalStore() {

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
		selectionInEditor= null;
	}

}
