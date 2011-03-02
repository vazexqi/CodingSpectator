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
public class UndoneTextChangeOperation extends TextChangeOperation {

	public UndoneTextChangeOperation() {
		super();
	}

	public UndoneTextChangeOperation(DocumentEvent documentEvent, String replacedText) {
		super(documentEvent, replacedText);
	}

	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.TEXT_CHANGE_UNDONE_SYMBOL;
	}

	@Override
	public String getDescription() {
		return "Undone text change";
	}

	@Override
	protected void replayTextChange() throws ExecutionException {
		getCurrentDocumentUndoManager().undo();
	}

}
