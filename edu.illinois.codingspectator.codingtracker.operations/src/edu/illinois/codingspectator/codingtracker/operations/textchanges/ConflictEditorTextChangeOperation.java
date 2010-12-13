/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.textchanges;

import org.eclipse.jface.text.TextEvent;

import edu.illinois.codingspectator.codingtracker.operations.OperationLexer;
import edu.illinois.codingspectator.codingtracker.operations.OperationTextChunk;

/**
 * 
 * @author Stas Negara
 * 
 */
public abstract class ConflictEditorTextChangeOperation extends TextChangeOperation {

	private String editorID;

	public ConflictEditorTextChangeOperation() {
		super();
	}

	public ConflictEditorTextChangeOperation(String editorID, TextEvent textEvent) {
		super(textEvent);
		this.editorID= editorID;
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		textChunk.append(editorID);
		super.populateTextChunk(textChunk);
	}

	@Override
	protected void initializeFrom(OperationLexer operationLexer) {
		editorID= operationLexer.getNextLexeme();
		super.initializeFrom(operationLexer);
	}

}
