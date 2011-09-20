/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.listeners.document;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;

import edu.illinois.codingtracker.helpers.Debugger;
import edu.illinois.codingtracker.helpers.Messages;
import edu.illinois.codingtracker.listeners.BasicListener;

/**
 * 
 * @author Stas Negara
 * 
 */
public abstract class DocumentListener extends BasicListener implements IDocumentListener {

	private static final String ERROR_TEXT= ",,,,,";

	protected String replacedText= "";

	protected String oldDocumentText= "";

	private DocumentEvent currentEvent= null;


	@Override
	public void documentAboutToBeChanged(DocumentEvent event) {
		currentEvent= event;
		try {
			IDocument document= event.getDocument();
			replacedText= document.get(event.getOffset(), event.getLength());
			oldDocumentText= document.get();
			astRecorder.beforeDocumentChange(event, getCurrentFilePath());
		} catch (BadLocationException e) {
			handleException(e, event, Messages.Recorder_BadDocumentLocation);
		}
	}

	@Override
	public void documentChanged(DocumentEvent event) {
		if (currentEvent != event) {
			handleException(new RuntimeException(), event, Messages.Recorder_UnsynchronizedDocumentNotifications);
		}
		handleDocumentChange(event);
	}

	private void handleException(Exception ex, DocumentEvent event, String message) {
		Debugger.logExceptionToErrorLog(ex, message + ": offset=" + event.getOffset() + ", length=" + event.getLength());
		replacedText= ERROR_TEXT;
		oldDocumentText= ERROR_TEXT;
	}

	protected abstract void handleDocumentChange(DocumentEvent event);

	protected abstract String getCurrentFilePath();

}
