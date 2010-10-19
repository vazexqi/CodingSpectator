/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.listeners;

import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;

import edu.illinois.codingspectator.codingtracker.EditorHelper;
import edu.illinois.codingspectator.codingtracker.Logger;
import edu.illinois.codingspectator.codingtracker.UserSessionState;

/**
 * 
 * @author Stas Negara
 * @author Mohsen Vakilian - Extracted this class from CodeChangeTracker
 * 
 */
public class PartListener implements IPartListener {

	UserSessionState userSessionState;

	public PartListener(UserSessionState userSessionState) {
		super();
		this.userSessionState= userSessionState;
	}

	@Override
	public void partActivated(IWorkbenchPart part) {
	}

	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
	}

	@Override
	public void partClosed(IWorkbenchPart part) {
		IFile closedFile= null;
		if (part instanceof CompareEditor) {
			closedFile= EditorHelper.getEditorJavaFile((CompareEditor)part);
		} else if (part instanceof AbstractDecoratedTextEditor) {
			closedFile= EditorHelper.getEditorJavaFile((AbstractDecoratedTextEditor)part);
		}
		if (closedFile != null) {
			if (EditorHelper.isConflictEditor((EditorPart)part)) {
				CompareEditor compareEditor= (CompareEditor)part;
				userSessionState.getOpenConflictEditors().remove(compareEditor);
				userSessionState.getDirtyConflictEditors().remove(compareEditor);
				Logger.getInstance().logClosedConflictEditor(EditorHelper.getConflictEditorID(compareEditor));
			} else {
				//Check that this is the last editor of this file that is closed
				IWorkbenchPage activePage= userSessionState.getActiveWorkbenchWindow().getActivePage();
				if (activePage != null) {
					IEditorReference[] editorReferences= activePage.getEditorReferences();
					for (IEditorReference editorReference : editorReferences) {
						IEditorPart editor= editorReference.getEditor(false);
						if (editor != part && !EditorHelper.isConflictEditor(editor)) {
							IFile file= null;
							if (editor instanceof CompareEditor) {
								file= EditorHelper.getEditorJavaFile((CompareEditor)editor);
							} else if (editor instanceof AbstractDecoratedTextEditor) {
								file= EditorHelper.getEditorJavaFile((AbstractDecoratedTextEditor)editor);
							}
							if (closedFile.equals(file)) {
								return; // file is not really closed as it is opened in another editor
							}
						}
					}
				}
				userSessionState.getDirtyFiles().remove(closedFile);
				Logger.getInstance().logClosedFile(closedFile);
			}
		}
	}

	@Override
	public void partDeactivated(IWorkbenchPart part) {
	}

	@Override
	public void partOpened(IWorkbenchPart part) {
	}

}
