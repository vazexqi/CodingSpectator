/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.conflicteditors;

import edu.illinois.codingspectator.codingtracker.helpers.Debugger;
import edu.illinois.codingspectator.codingtracker.operations.CompareEditorsUpkeeper;
import edu.illinois.codingspectator.codingtracker.operations.OperationLexer;
import edu.illinois.codingspectator.codingtracker.operations.OperationSymbols;
import edu.illinois.codingspectator.codingtracker.operations.OperationTextChunk;

/**
 * 
 * @author Stas Negara
 * 
 */
public class SavedConflictEditorOperation extends ConflictEditorOperation {

	//TODO: Code that involves 'success' is very similar to the one from BreakableResourceOperation
	private boolean success;

	public SavedConflictEditorOperation() {
		super();
	}

	public SavedConflictEditorOperation(String editorID, boolean success) {
		super(editorID);
		this.success= success;
	}

	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.CONFLICT_EDITOR_SAVED_SYMBOL;
	}

	@Override
	public String getDescription() {
		return "Saved conflict editor";
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		super.populateTextChunk(textChunk);
		textChunk.append(success);
	}

	@Override
	protected void initializeFrom(OperationLexer operationLexer) {
		super.initializeFrom(operationLexer);
		success= operationLexer.readBoolean();
	}

	@SuppressWarnings("restriction")
	@Override
	public void replay() {
		if (success) {
			CompareEditorsUpkeeper.getEditor(editorID).doSave(null);
			//FIXME: Instead of sleeping, should listen to IProgressMonitor.done()
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				//do nothing
			}
		} else {
			Debugger.debugWarning("Ignored unsuccessful save of the conflict editor: " + this);
		}
	}

	@Override
	public String toString() {
		StringBuffer sb= new StringBuffer();
		sb.append("Success: " + success + "\n");
		sb.append(super.toString());
		return sb.toString();
	}

}
