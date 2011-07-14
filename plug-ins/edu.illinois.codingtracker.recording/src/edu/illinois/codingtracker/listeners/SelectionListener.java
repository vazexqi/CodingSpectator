/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.listeners;

import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import edu.illinois.codingtracker.helpers.Debugger;
import edu.illinois.codingtracker.helpers.EditorHelper;
import edu.illinois.codingtracker.listeners.document.ConflictEditorDocumentListener;

/**
 * 
 * @author Stas Negara
 * 
 */
@SuppressWarnings("restriction")
public class SelectionListener extends BasicListener implements ISelectionListener {

	public static void register() {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				boolean isSelectionListenerRegistered= false;
				long currentTimeMillis= System.currentTimeMillis();
				while (!isSelectionListenerRegistered) {
					activeWorkbenchWindow= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					if (activeWorkbenchWindow != null) {
						activeWorkbenchWindow.getSelectionService().addSelectionListener(new SelectionListener());
						isSelectionListenerRegistered= true;
					}
				}
				long currentTimeMillis2= System.currentTimeMillis();
				System.err.println("Duration: " + (currentTimeMillis2 - currentTimeMillis));
			}
		});
	}


	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		Debugger.debugWorkbenchPart("Selected part: ", part);
		if (EditorHelper.isConflictEditor(part)) {
			CompareEditor compareEditor= (CompareEditor)part;
			IFile editedFile= EditorHelper.getEditedJavaFile(compareEditor);
			if (editedFile != null) {
				handleConflictEditorSelection(compareEditor, editedFile, EditorHelper.getEditingSourceViewer(compareEditor));
			}
		}
	}


	private void handleConflictEditorSelection(CompareEditor compareEditor, IFile newFile, ISourceViewer sourceViewer) {
		if (!openConflictEditors.contains(compareEditor)) {
			openConflictEditors.add(compareEditor);
			dirtyConflictEditors.add(compareEditor); //conflict editors are always dirty from the start
			operationRecorder.recordOpenedConflictEditor(EditorHelper.getConflictEditorID(compareEditor), newFile, EditorHelper.getConflictEditorInitialContent(compareEditor));
			if (sourceViewer != null && sourceViewer.getDocument() != null) {
				sourceViewer.getDocument().addDocumentListener(new ConflictEditorDocumentListener(compareEditor));
			}
		}
	}

}
