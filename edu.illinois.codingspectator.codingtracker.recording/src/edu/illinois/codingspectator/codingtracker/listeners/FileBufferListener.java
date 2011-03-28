/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.listeners;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.IFileBufferListener;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;

import edu.illinois.codingspectator.codingtracker.helpers.FileHelper;

/**
 * 
 * @author Stas Negara
 * 
 */
public class FileBufferListener extends BasicListener implements IFileBufferListener {

	private final DocumentListener documentListener= new DocumentListener();

	public static void register() {
		FileBuffers.getTextFileBufferManager().addFileBufferListener(new FileBufferListener());
	}

	@Override
	public void bufferContentAboutToBeReplaced(IFileBuffer buffer) {
		IResource replacedResource= FileHelper.findWorkspaceMember(buffer.getLocation());
		if (replacedResource instanceof IFile && replacedResource.exists()) {
			if (buffer instanceof ITextFileBuffer) {//actually, should always be ITextFileBuffer
				addDocumentListener((ITextFileBuffer)buffer);
				replacedFile= (IFile)replacedResource;
			}
		}
	}

	@Override
	public void bufferContentReplaced(IFileBuffer buffer) {
		replacedFile= null;
		if (buffer instanceof ITextFileBuffer) {//actually, should always be ITextFileBuffer
			removeDocumentListener((ITextFileBuffer)buffer);
		}
	}

	private void addDocumentListener(ITextFileBuffer textFileBuffer) {
		IDocument textFileBufferDocument= textFileBuffer.getDocument();
		//Add document listener only if the document is not current in order to avoid double listening.
		if (textFileBufferDocument != currentDocument) {
			textFileBufferDocument.addDocumentListener(documentListener);
		}
	}

	private void removeDocumentListener(ITextFileBuffer textFileBuffer) {
		textFileBuffer.getDocument().removeDocumentListener(documentListener);
	}

	@Override
	public void bufferCreated(IFileBuffer buffer) {
		//do nothing
	}

	@Override
	public void bufferDisposed(IFileBuffer buffer) {
		//do nothing
	}

	@Override
	public void stateChanging(IFileBuffer buffer) {
		//do nothing
	}

	@Override
	public void dirtyStateChanged(IFileBuffer buffer, boolean isDirty) {
		//do nothing
	}

	@Override
	public void stateValidationChanged(IFileBuffer buffer, boolean isStateValidated) {
		//do nothing
	}

	@Override
	public void underlyingFileMoved(IFileBuffer buffer, IPath path) {
		//do nothing
	}

	@Override
	public void underlyingFileDeleted(IFileBuffer buffer) {
		//do nothing
	}

	@Override
	public void stateChangeFailed(IFileBuffer buffer) {
		//do nothing
	}

}
