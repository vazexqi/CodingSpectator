/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.starts;

import edu.illinois.codingspectator.codingtracker.operations.OperationSymbols;
import edu.illinois.codingspectator.codingtracker.operations.OperationTextChunk;
import edu.illinois.codingspectator.codingtracker.operations.UserOperation;

/**
 * 
 * @author Stas Negara
 * 
 */
public class StartEclipseOperation extends UserOperation {

	public StartEclipseOperation() {
		super();
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		//Nothing to populate here
	}

	@Override
	protected String getOperationSymbol() {
		return OperationSymbols.ECLIPSE_SESSION_SYMBOL;
	}

	@Override
	protected String getDebugMessage() {
		return "Eclipse started: ";
	}

}
