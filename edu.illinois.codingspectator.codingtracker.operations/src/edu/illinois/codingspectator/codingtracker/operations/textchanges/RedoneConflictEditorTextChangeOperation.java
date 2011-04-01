/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.textchanges;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.DocumentEvent;

import edu.illinois.codingspectator.codingtracker.operations.OperationSymbols;

/**
 * 
 * @author Stas Negara
 * 
 */
public class RedoneConflictEditorTextChangeOperation extends ConflictEditorTextChangeOperation {

	public RedoneConflictEditorTextChangeOperation() {
		super();
	}

	public RedoneConflictEditorTextChangeOperation(String editorID, DocumentEvent documentEvent, String replacedText) {
		super(editorID, documentEvent, replacedText);
	}

	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.CONFLICT_EDITOR_TEXT_CHANGE_REDONE_SYMBOL;
	}

	@Override
	public String getDescription() {
		return "Redone conflict editor text change";
	}

	@Override
	protected void replayTextChange() throws ExecutionException {
		getCurrentDocumentUndoManager().redo();
	}

}
