/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.conflicteditors;

import org.eclipse.core.resources.IFile;

import edu.illinois.codingspectator.codingtracker.helpers.RecorderHelper;
import edu.illinois.codingspectator.codingtracker.operations.OperationSymbols;
import edu.illinois.codingspectator.codingtracker.operations.OperationTextChunk;

/**
 * 
 * @author Stas Negara
 * 
 */
public class OpenedConflictEditorOperation extends ConflictEditorOperation {

	private final IFile editedFile;

	private final String initialContent;

	public OpenedConflictEditorOperation(String editorID, IFile editedFile, String initialContent) {
		super(editorID);
		this.editedFile= editedFile;
		this.initialContent= initialContent;
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		super.populateTextChunk(textChunk);
		textChunk.append(RecorderHelper.getPortableFilePath(editedFile));
		textChunk.append(initialContent);
	}

	@Override
	protected String getOperationSymbol() {
		return OperationSymbols.CONFLICT_EDITOR_OPENED_SYMBOL;
	}

	@Override
	protected String getDebugMessage() {
		return "Conflict editor opened: ";
	}

}
