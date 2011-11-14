/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.textchanges;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;

import edu.illinois.codingtracker.operations.OperationSymbols;

/**
 * 
 * @author Stas Negara
 * 
 */
public class UndoneTextChangeOperation extends TextChangeOperation {

	//This flag allows to track replaying of undone text changes.
	public static boolean isReplaying= false;


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
	public void replay() throws BadLocationException, ExecutionException {
		isReplaying= true;
		super.replay();
		isReplaying= false;
	}

	@Override
	protected void replaySpecificTextChange() throws ExecutionException {
		getCurrentDocumentUndoManager().undo();
	}

}
