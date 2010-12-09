/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.textchanges;

import org.eclipse.jface.text.TextEvent;

import edu.illinois.codingspectator.codingtracker.recording.TextChunk;

/**
 * 
 * @author Stas Negara
 * 
 * 
 */
public abstract class ConflictEditorTextChangeOperation extends TextChangeOperation {

	private final String editorID;

	public ConflictEditorTextChangeOperation(String editorID, TextEvent textEvent, String operationSymbol, String debugMessage) {
		super(textEvent, operationSymbol, debugMessage);
		this.editorID= editorID;
	}

	@Override
	protected void populateTextChunk(TextChunk textChunk) {
		textChunk.append(editorID);
		super.populateTextChunk(textChunk);
	}

}
