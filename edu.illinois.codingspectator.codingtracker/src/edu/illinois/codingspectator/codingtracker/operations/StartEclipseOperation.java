/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations;

import edu.illinois.codingspectator.codingtracker.recording.Symbols;
import edu.illinois.codingspectator.codingtracker.recording.TextChunk;

/**
 * 
 * @author Stas Negara
 * 
 * 
 */
public class StartEclipseOperation extends UserOperation {

	public StartEclipseOperation() {
		super(Symbols.ECLIPSE_SESSION_SYMBOL, "Eclipse started: ");
	}

	@Override
	protected void populateTextChunk(TextChunk textChunk) {
		//Nothing to populate here
	}

}
