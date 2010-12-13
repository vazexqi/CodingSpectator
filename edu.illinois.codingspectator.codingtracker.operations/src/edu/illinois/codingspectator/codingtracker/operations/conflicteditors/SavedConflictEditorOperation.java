/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.conflicteditors;

import edu.illinois.codingspectator.codingtracker.operations.OperationSymbols;

/**
 * 
 * @author Stas Negara
 * 
 */
public class SavedConflictEditorOperation extends ConflictEditorOperation {

	public SavedConflictEditorOperation() {
		super();
	}

	public SavedConflictEditorOperation(String editorID) {
		super(editorID);
	}

	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.CONFLICT_EDITOR_SAVED_SYMBOL;
	}

	@Override
	protected String getDebugMessage() {
		return "Conflict editor saved: ";
	}

}
