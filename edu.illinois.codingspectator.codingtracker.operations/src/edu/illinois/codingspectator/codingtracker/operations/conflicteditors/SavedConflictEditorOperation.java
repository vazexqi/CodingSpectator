/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.conflicteditors;

import edu.illinois.codingspectator.codingtracker.operations.CompareEditorsUpkeeper;
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
	public String getDescription() {
		return "Saved conflict editor";
	}

	@SuppressWarnings("restriction")
	@Override
	public void replay() {
		CompareEditorsUpkeeper.getEditor(editorID).doSave(null);
		//FIXME: Instead of sleeping, should listen to IProgressMonitor.done()
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			//do nothing
		}
	}

}
