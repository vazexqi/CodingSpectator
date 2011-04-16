/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.textchanges;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.ui.IEditorPart;

import edu.illinois.codingspectator.codingtracker.operations.CompareEditorsUpkeeper;
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

	public ConflictEditorTextChangeOperation(String editorID, DocumentEvent documentEvent, String replacedText) {
		super(documentEvent, replacedText);
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

	@Override
	public void replay() throws ExecutionException, BadLocationException {
		IEditorPart oldEditor= currentEditor;
		currentEditor= CompareEditorsUpkeeper.getEditor(editorID);
		super.replay();
		currentEditor= oldEditor;
	}

	@Override
	public String toString() {
		StringBuffer sb= new StringBuffer();
		sb.append("Editor ID: " + editorID + "\n");
		sb.append(super.toString());
		return sb.toString();
	}

}
