/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations;

import org.eclipse.core.resources.IFile;

import edu.illinois.codingspectator.codingtracker.helpers.RecorderHelper;
import edu.illinois.codingspectator.codingtracker.recording.Symbols;
import edu.illinois.codingspectator.codingtracker.recording.TextChunk;

/**
 * 
 * @author Stas Negara
 * 
 * 
 */
public class FileEditOperation extends UserOperation {

	private final IFile editedFile;

	public FileEditOperation(IFile editedFile) {
		super(Symbols.FILE_EDIT_SYMBOL, "File edited: ");
		this.editedFile= editedFile;
	}

	@Override
	protected void populateTextChunk(TextChunk textChunk) {
		textChunk.append(RecorderHelper.getPortableFilePath(editedFile));
	}

}
