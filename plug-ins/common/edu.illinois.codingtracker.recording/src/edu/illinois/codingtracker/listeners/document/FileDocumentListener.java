/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.listeners.document;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.DocumentEvent;

import edu.illinois.codingtracker.helpers.ResourceHelper;

/**
 * 
 * @author Stas Negara
 * 
 */
public class FileDocumentListener extends DocumentListener {

	private final IFile documentFile;


	public FileDocumentListener(IFile documentFile) {
		this.documentFile= documentFile;
	}

	@Override
	protected String getCurrentFilePath() {
		return ResourceHelper.getPortableResourcePath(documentFile);
	}

	@Override
	protected void handleDocumentChange(DocumentEvent event) {
		if (isFileRefreshed(event)) {
			operationRecorder.recordRefreshedFile(documentFile, replacedText);
		} else {
			operationRecorder.recordChangedText(event, replacedText, oldDocumentText, documentFile, isUndoing, isRedoing);
		}
	}

	/**
	 * File is considered to be refreshed when the corresponding document is about to be replaced
	 * and the file's content coincides with the current document text and the replacing document
	 * text.
	 * 
	 * @param documentEvent
	 * @return
	 */
	private boolean isFileRefreshed(DocumentEvent documentEvent) {
		if (isBufferContentAboutToBeReplaced) {
			String currentDocumentText= documentEvent.getDocument().get();
			return currentDocumentText.equals(ResourceHelper.readFileContent(documentFile)) && currentDocumentText.equals(documentEvent.getText());
		}
		return false;
	}

}
