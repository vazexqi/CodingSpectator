/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.conflicteditors;

import org.eclipse.core.resources.IFile;

import edu.illinois.codingspectator.codingtracker.helpers.FileHelper;
import edu.illinois.codingspectator.codingtracker.operations.OperationLexer;
import edu.illinois.codingspectator.codingtracker.operations.OperationSymbols;
import edu.illinois.codingspectator.codingtracker.operations.OperationTextChunk;

/**
 * 
 * @author Stas Negara
 * 
 */
public class OpenedConflictEditorOperation extends ConflictEditorOperation {

	private String editedFilePath;

	private String initialContent;

	public OpenedConflictEditorOperation() {
		super();
	}

	public OpenedConflictEditorOperation(String editorID, IFile editedFile, String initialContent) {
		super(editorID);
		this.editedFilePath= FileHelper.getPortableFilePath(editedFile);
		this.initialContent= initialContent;
	}

	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.CONFLICT_EDITOR_OPENED_SYMBOL;
	}

	@Override
	protected String getDebugMessage() {
		return "Conflict editor opened: ";
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		super.populateTextChunk(textChunk);
		textChunk.append(editedFilePath);
		textChunk.append(initialContent);
	}

	@Override
	protected void initializeFrom(OperationLexer operationLexer) {
		super.initializeFrom(operationLexer);
		editedFilePath= operationLexer.getNextLexeme();
		initialContent= operationLexer.getNextLexeme();
	}

}
