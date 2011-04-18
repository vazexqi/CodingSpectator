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
import edu.illinois.codingspectator.codingtracker.listeners.document.FileDocumentListener;

/**
 * 
 * @author Stas Negara
 * 
 */
public class FileBufferListener extends BasicListener implements IFileBufferListener {

	public static void register() {
		FileBuffers.getTextFileBufferManager().addFileBufferListener(new FileBufferListener());
	}

	@Override
	public void bufferContentAboutToBeReplaced(IFileBuffer buffer) {
		isBufferContentAboutToBeReplaced= true;
	}

	@Override
	public void bufferContentReplaced(IFileBuffer buffer) {
		isBufferContentAboutToBeReplaced= false;
	}

	@Override
	public void bufferCreated(IFileBuffer buffer) {
		if (buffer instanceof ITextFileBuffer) {//actually, should always be ITextFileBuffer
			addDocumentListener((ITextFileBuffer)buffer);
		}
	}

	private void addDocumentListener(ITextFileBuffer textFileBuffer) {
		//TODO: Check that there is no need to listen to buffers without the corresponding workspace resources.
		IResource bufferResource= FileHelper.findWorkspaceMember(textFileBuffer.getLocation());
		if (bufferResource instanceof IFile && bufferResource.exists()) {
			IDocument textFileBufferDocument= textFileBuffer.getDocument();
			textFileBufferDocument.addDocumentListener(new FileDocumentListener((IFile)bufferResource));
		}
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
