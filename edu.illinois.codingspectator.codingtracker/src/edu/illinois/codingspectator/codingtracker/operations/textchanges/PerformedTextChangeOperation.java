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
public class PerformedTextChangeOperation extends TextChangeOperation {

	public PerformedTextChangeOperation(TextEvent textEvent) {
		super(textEvent);
	}

	@Override
	protected String getOperationSymbol() {
		return OperationSymbols.TEXT_CHANGE_PERFORMED_SYMBOL;
	}

	@Override
	protected String getDebugMessage() {
		return "Performed text change: ";
	}

}
