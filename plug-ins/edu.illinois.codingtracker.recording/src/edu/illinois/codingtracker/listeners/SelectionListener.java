/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.listeners;

import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

import edu.illinois.codingtracker.helpers.Debugger;
import edu.illinois.codingtracker.helpers.EditorHelper;
import edu.illinois.codingtracker.listeners.document.ConflictEditorDocumentListener;

/**
 * 
 * @author Stas Negara
 * @author Mohsen Vakilian, nchen - Registered the listener asynchronously.
 * 
 */
@SuppressWarnings("restriction")
public class SelectionListener extends BasicListener implements ISelectionListener {

	public static void register() {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				boolean isSelectionListenerRegistered= false;
				while (!isSelectionListenerRegistered) {
					IWorkbenchWindow activeWorkbenchWindow= BasicListener.getActiveWorkbenchWindow();
					if (activeWorkbenchWindow != null) {
						activeWorkbenchWindow.getSelectionService().addSelectionListener(new SelectionListener());
						isSelectionListenerRegistered= true;
					}
				}
			}
		});
	}


	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		Debugger.debugWorkbenchPart("Selected part: ", part);
		if (EditorHelper.isConflictEditor(part)) {
			CompareEditor compareEditor= (CompareEditor)part;
			IFile editedFile= EditorHelper.getEditedJavaFile(compareEditor);
			handleConflictEditorSelection(compareEditor, editedFile, EditorHelper.getEditingSourceViewer(compareEditor));
		}
	}


	/**
	 * Note that editedFile might be null.
	 * 
	 * @param compareEditor
	 * @param editedFile
	 * @param sourceViewer
	 */
	private void handleConflictEditorSelection(CompareEditor compareEditor, IFile editedFile, ISourceViewer sourceViewer) {
		if (!openConflictEditors.contains(compareEditor)) {
			openConflictEditors.add(compareEditor);
			IDocument editedDocument= sourceViewer != null ? sourceViewer.getDocument() : null;
			String initialContent= "";
			if (editedFile != null) {
				initialContent= EditorHelper.getConflictEditorInitialContent(compareEditor);
			} else if (editedDocument != null) {
				initialContent= editedDocument.get();
			}
			operationRecorder.recordOpenedConflictEditor(EditorHelper.getConflictEditorID(compareEditor), editedFile, initialContent);
			if (editedDocument != null) {
				editedDocument.addDocumentListener(new ConflictEditorDocumentListener(compareEditor));
			}
		}
	}
}
