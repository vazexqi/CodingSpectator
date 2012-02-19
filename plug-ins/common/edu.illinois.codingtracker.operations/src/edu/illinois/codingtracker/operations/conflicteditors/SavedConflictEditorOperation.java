/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.conflicteditors;

import org.eclipse.compare.internal.CompareEditor;

import edu.illinois.codingtracker.helpers.Configuration;
import edu.illinois.codingtracker.helpers.Debugger;
import edu.illinois.codingtracker.compare.helpers.EditorHelper;
import edu.illinois.codingtracker.operations.OperationLexer;
import edu.illinois.codingtracker.operations.OperationSymbols;
import edu.illinois.codingtracker.operations.OperationTextChunk;

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
		if (!Configuration.isOldFormat) {
			success= operationLexer.readBoolean();
		} else {
			success= true;
		}
	}

	@SuppressWarnings("restriction")
	@Override
	public void replay() {
		if (success) {
			CompareEditor compareEditor= EditorHelper.getCompareEditor(editorID);
			if (compareEditor == null) {
				Debugger.debugWarning("Can not save non existing conflict editor:\n" + this);
			} else {
				compareEditor.doSave(null);
			}
			//FIXME: Instead of sleeping, should listen to IProgressMonitor.done()
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				//do nothing
			}
		} else {
			Debugger.debugWarning("Ignored unsuccessful save of the conflict editor:\n" + this);
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
