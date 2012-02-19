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
public class PerformedTextChangeOperation extends TextChangeOperation {

	public PerformedTextChangeOperation() {
		super();
	}

	public PerformedTextChangeOperation(DocumentEvent documentEvent, String replacedText) {
		super(documentEvent, replacedText);
	}

	public PerformedTextChangeOperation(DocumentEvent documentEvent, String replacedText, long timestamp) {
		super(documentEvent, replacedText, timestamp);
	}

	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.TEXT_CHANGE_PERFORMED_SYMBOL;
	}

	@Override
	public String getDescription() {
		return "Performed text change";
	}

	@Override
	protected void replaySpecificTextChange() throws BadLocationException {
		currentDocument.replace(offset, length, newText);
	}

}
