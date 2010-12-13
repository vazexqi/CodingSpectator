/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.textchanges;

import org.eclipse.jface.text.TextEvent;

import edu.illinois.codingspectator.codingtracker.operations.OperationSymbols;

/**
 * 
 * @author Stas Negara
 * 
 */
public class PerformedConflictEditorTextChangeOperation extends ConflictEditorTextChangeOperation {

	public PerformedConflictEditorTextChangeOperation() {
		super();
	}

	public PerformedConflictEditorTextChangeOperation(String editorID, TextEvent textEvent) {
		super(editorID, textEvent);
	}

	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.CONFLICT_EDITOR_TEXT_CHANGE_PERFORMED_SYMBOL;
	}

	@Override
	protected String getDebugMessage() {
		return "Performed conflict editor text change: ";
	}

}
