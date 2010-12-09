/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.files;

import java.io.File;

import org.eclipse.core.resources.IFile;

import edu.illinois.codingspectator.codingtracker.helpers.RecorderHelper;
import edu.illinois.codingspectator.codingtracker.operations.OperationTextChunk;

/**
 * 
 * @author Stas Negara
 * 
 */
public abstract class SnapshotedFileOperation extends FileOperation {

	private final String fileContent;

	public SnapshotedFileOperation(IFile snapshotedFile) {
		super(snapshotedFile);
		fileContent= RecorderHelper.getFileContent(new File(snapshotedFile.getLocation().toOSString()));
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		super.populateTextChunk(textChunk);
		textChunk.append(fileContent);
	}

}
