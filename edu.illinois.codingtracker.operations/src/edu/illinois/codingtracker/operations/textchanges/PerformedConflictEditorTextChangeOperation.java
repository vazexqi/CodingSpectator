/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.textchanges;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;

import edu.illinois.codingtracker.operations.OperationSymbols;

/**
 * 
 * @author Stas Negara
 * 
 */
public class PerformedConflictEditorTextChangeOperation extends ConflictEditorTextChangeOperation {

	public PerformedConflictEditorTextChangeOperation() {
		super();
	}

	public PerformedConflictEditorTextChangeOperation(String editorID, DocumentEvent documentEvent, String replacedText) {
		super(editorID, documentEvent, replacedText);
	}

	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.CONFLICT_EDITOR_TEXT_CHANGE_PERFORMED_SYMBOL;
	}

	@Override
	public String getDescription() {
		return "Performed conflict editor text change";
	}

	@Override
	protected void replaySpecificTextChange() throws BadLocationException {
		currentDocument.replace(offset, length, newText);
	}

}
