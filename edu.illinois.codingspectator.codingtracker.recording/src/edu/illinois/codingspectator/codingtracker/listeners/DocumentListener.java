/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.listeners;

import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;

import edu.illinois.codingspectator.codingtracker.helpers.Debugger;
import edu.illinois.codingspectator.codingtracker.helpers.EditorHelper;
import edu.illinois.codingspectator.codingtracker.helpers.FileHelper;
import edu.illinois.codingspectator.codingtracker.helpers.Messages;

/**
 * 
 * @author Stas Negara
 * 
 */
@SuppressWarnings("restriction")
public class DocumentListener extends BasicListener implements IDocumentListener {

	private static final String ERROR_REPLACED_TEXT= ",,,,,";

	private String replacedText= "";

	private DocumentEvent currentEvent= null;

	@Override
	public void documentAboutToBeChanged(DocumentEvent event) {
//		if (!isRefactoring) {
		currentEvent= event;
		try {
			replacedText= event.getDocument().get(event.getOffset(), event.getLength());
		} catch (BadLocationException e) {
			handleException(e, event, Messages.Recorder_BadDocumentLocation);
		}
//		}
	}

	@Override
	public void documentChanged(DocumentEvent event) {
//		if (!isRefactoring) {
		if (currentEvent != event) {
			handleException(new RuntimeException(), event, Messages.Recorder_UnsynchronizedDocumentNotifications);
		}
		if (isFileRefreshed(event)) {
			operationRecorder.recordRefreshedFile(replacedFile, replacedText);
		} else if (event.getDocument() == currentDocument) { //If the file is not refreshed, process events only for the current document.
			if (EditorHelper.isConflictEditor(currentEditor)) {
				CompareEditor compareEditor= (CompareEditor)currentEditor;
				dirtyConflictEditors.add(compareEditor);
				operationRecorder.recordConflictEditorChangedText(event, replacedText, EditorHelper.getConflictEditorID(compareEditor), isUndoing, isRedoing);
			} else {
				operationRecorder.recordChangedText(event, replacedText, currentFile, isUndoing, isRedoing);
			}
		}
//		}
	}

	/**
	 * File is considered to be refreshed when the corresponding document is replaced (which is when
	 * replacedFile != null), the file is not known and the file's content coincides with the
	 * current document text and the replacing document text.
	 * 
	 * @param documentEvent
	 * @return
	 */
	private boolean isFileRefreshed(DocumentEvent documentEvent) {
		if (replacedFile != null && !knownfilesRecorder.isFileKnown(replacedFile)) {
			String currentDocumentText= documentEvent.getDocument().get();
			return currentDocumentText.equals(FileHelper.readFileContent(replacedFile)) && currentDocumentText.equals(documentEvent.getText());
		}
		return false;
	}

	private void handleException(Exception ex, DocumentEvent event, String message) {
		Debugger.logExceptionToErrorLog(ex, message + ": offset=" + event.getOffset() + ", length=" + event.getLength());
		replacedText= ERROR_REPLACED_TEXT;
	}

}
