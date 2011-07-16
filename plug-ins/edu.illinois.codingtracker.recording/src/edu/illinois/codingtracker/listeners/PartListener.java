/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.listeners;

import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.core.resources.IFile;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;

import edu.illinois.codingtracker.helpers.EditorHelper;
import edu.illinois.codingtracker.recording.Activator;

/**
 * 
 * @author Stas Negara
 * @author Mohsen Vakilian - Extracted this class from CodeChangeTracker
 * 
 */
@SuppressWarnings("restriction")
public class PartListener extends BasicListener implements IPartListener {

	public static void register() {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				//TODO: Is it too heavy-weight? Did not notice any additional lag even on a slow machine.  
				boolean isPartListenerRegistered= false;
				while (!isPartListenerRegistered) {
					IWorkbenchWindow activeWorkbenchWindow= BasicListener.getActiveWorkbenchWindow();
					if (activeWorkbenchWindow != null) {
						IWorkbenchPage activePage= activeWorkbenchWindow.getActivePage();
						if (activePage != null) {
							activePage.addPartListener(new PartListener());
							isPartListenerRegistered= true;
						}
					}
				}
			}
		});
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
			closedFile= EditorHelper.getEditedJavaFile((CompareEditor)part);
		} else if (part instanceof AbstractDecoratedTextEditor) {
			closedFile= EditorHelper.getEditedJavaFile((AbstractDecoratedTextEditor)part);
		}
		if (closedFile != null) {
			if (EditorHelper.isConflictEditor(part)) {
				closeConflictEditor((CompareEditor)part);
			} else {
				closeRegularEditor(part, closedFile);
			}
		}
	}

	private void closeConflictEditor(CompareEditor compareEditor) {
		openConflictEditors.remove(compareEditor);
		dirtyConflictEditors.remove(compareEditor);
		operationRecorder.recordClosedConflictEditor(EditorHelper.getConflictEditorID(compareEditor));
	}

	private void closeRegularEditor(IWorkbenchPart part, IFile closedFile) {
		//Check that this is the last editor of this file that is closed
		IWorkbenchWindow activeWorkbenchWindow= BasicListener.getActiveWorkbenchWindow();
		if (activeWorkbenchWindow == null) {
			Activator.getDefault().log(Activator.createErrorStatus("Workbench has not been created yet.", new RuntimeException()));
		}
		IWorkbenchPage activePage= activeWorkbenchWindow.getActivePage();
		if (activePage != null) {
			IEditorReference[] editorReferences= activePage.getEditorReferences();
			for (IEditorReference editorReference : editorReferences) {
				IEditorPart editor= editorReference.getEditor(false);
				if (editor != part && !EditorHelper.isConflictEditor(editor)) {
					IFile file= null;
					if (editor instanceof CompareEditor) {
						file= EditorHelper.getEditedJavaFile((CompareEditor)editor);
					} else if (editor instanceof AbstractDecoratedTextEditor) {
						file= EditorHelper.getEditedJavaFile((AbstractDecoratedTextEditor)editor);
					}
					if (closedFile.equals(file)) {
						return; // file is not really closed as it is opened in another editor
					}
				}
			}
		}
		operationRecorder.recordClosedFile(closedFile);
	}

	@Override
	public void partDeactivated(IWorkbenchPart part) {
	}

	@Override
	public void partOpened(IWorkbenchPart part) {
	}


}
