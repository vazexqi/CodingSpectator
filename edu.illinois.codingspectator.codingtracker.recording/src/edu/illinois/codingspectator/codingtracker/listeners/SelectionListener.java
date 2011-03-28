/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.listeners;

import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;

import edu.illinois.codingspectator.codingtracker.helpers.Debugger;
import edu.illinois.codingspectator.codingtracker.helpers.EditorHelper;
import edu.illinois.codingspectator.codingtracker.helpers.Messages;

/**
 * 
 * @author Stas Negara
 * 
 */
@SuppressWarnings("restriction")
public class SelectionListener extends BasicListener implements ISelectionListener {

	private ISourceViewer currentViewer= null;

	private final DocumentListener documentListener= new DocumentListener();

	public static void register() {
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				activeWorkbenchWindow= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (activeWorkbenchWindow == null) {
					Exception e= new RuntimeException();
					Debugger.logExceptionToErrorLog(e, Messages.CodeChangeTracker_FailedToGetActiveWorkbenchWindow);
				}
			}
		});
		activeWorkbenchWindow.getSelectionService().addSelectionListener(new SelectionListener());
	}


	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		Debugger.debugWorkbenchPart("Selected part: ", part);
		IFile newFile= null;
		ISourceViewer sourceViewer= null;
		if (part instanceof CompareEditor) {
			CompareEditor compareEditor= (CompareEditor)part;
			newFile= EditorHelper.getEditedJavaFile(compareEditor);
			sourceViewer= EditorHelper.getEditingSourceViewer(compareEditor);
		} else if (part instanceof AbstractDecoratedTextEditor) {
			AbstractDecoratedTextEditor editor= (AbstractDecoratedTextEditor)part;
			newFile= EditorHelper.getEditedJavaFile(editor);
			sourceViewer= EditorHelper.getEditingSourceViewer(editor);
		}
		updateCurrentState(part, newFile, sourceViewer);
	}


	private void updateCurrentState(IWorkbenchPart part, IFile newFile, ISourceViewer sourceViewer) {
		if (newFile != null) {
			currentEditor= (EditorPart)part; //Should be EditorPart if newFile != null
			addEditor(currentEditor, newFile);
			if (!newFile.equals(currentFile)) {
				currentFile= newFile;
				Debugger.debugFilePath("Current file: ", currentFile);
			}
			if (currentDocument != null) {
				currentDocument.removeDocumentListener(documentListener);
				currentDocument= null;
			}
			currentViewer= sourceViewer;
			if (currentViewer != null) {
				currentDocument= currentViewer.getDocument();
			}
			if (currentDocument != null) {
				currentDocument.addDocumentListener(documentListener);
			}
		}
	}


	private void addEditor(EditorPart editor, IFile editedFile) {
		if (EditorHelper.isConflictEditor(editor)) {
			CompareEditor compareEditor= (CompareEditor)editor;
			if (!openConflictEditors.contains(compareEditor)) {
				openConflictEditors.add(compareEditor);
				dirtyConflictEditors.add(compareEditor); //conflict editors are always dirty from the start
				operationRecorder.recordOpenedConflictEditor(EditorHelper.getConflictEditorID(compareEditor), editedFile, EditorHelper.getConflictEditorInitialContent(compareEditor));
			}
		}
	}


}
