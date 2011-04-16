/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.conflicteditors;

import org.eclipse.compare.internal.CompareEditor;

import edu.illinois.codingspectator.codingtracker.operations.CompareEditorsUpkeeper;
import edu.illinois.codingspectator.codingtracker.operations.OperationSymbols;

/**
 * 
 * @author Stas Negara
 * 
 */
@SuppressWarnings("restriction")
public class ClosedConflictEditorOperation extends ConflictEditorOperation {

	public ClosedConflictEditorOperation() {
		super();
	}

	public ClosedConflictEditorOperation(String editorID) {
		super(editorID);
	}

	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.CONFLICT_EDITOR_CLOSED_SYMBOL;
	}

	@Override
	public String getDescription() {
		return "Closed conflict editor";
	}

	@Override
	public void replay() {
		CompareEditor compareEditor= CompareEditorsUpkeeper.getEditor(editorID);
		//Don't use getEditor().close(false), because it is executed asynchronously 
		compareEditor.getSite().getPage().closeEditor(compareEditor, false);
		CompareEditorsUpkeeper.removeEditor(editorID);
	}

}
