/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.textchanges;

import org.eclipse.jface.text.TextEvent;

import edu.illinois.codingspectator.codingtracker.recording.Symbols;

/**
 * 
 * @author Stas Negara
 * 
 * 
 */
public class PerformedTextChangeOperation extends TextChangeOperation {

	public PerformedTextChangeOperation(TextEvent textEvent) {
		super(textEvent, Symbols.TEXT_CHANGE_PERFORMED_SYMBOL, "Performed text change: ");
	}

}
