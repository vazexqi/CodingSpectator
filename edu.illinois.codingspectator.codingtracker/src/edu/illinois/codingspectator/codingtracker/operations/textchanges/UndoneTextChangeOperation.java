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
public class UndoneTextChangeOperation extends TextChangeOperation {

	public UndoneTextChangeOperation(TextEvent textEvent) {
		super(textEvent, Symbols.TEXT_CHANGE_UNDONE_SYMBOL, "Undone text change: ");
	}

}
