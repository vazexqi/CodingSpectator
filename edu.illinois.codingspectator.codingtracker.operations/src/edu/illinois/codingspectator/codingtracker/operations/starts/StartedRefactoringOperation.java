/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.starts;

import edu.illinois.codingspectator.codingtracker.operations.OperationLexer;
import edu.illinois.codingspectator.codingtracker.operations.OperationSymbols;
import edu.illinois.codingspectator.codingtracker.operations.OperationTextChunk;
import edu.illinois.codingspectator.codingtracker.operations.UserOperation;

/**
 * 
 * @author Stas Negara
 * 
 */
public class StartedRefactoringOperation extends UserOperation {

	public StartedRefactoringOperation() {
		super();
	}

	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.REFACTORING_STARTED_SYMBOL;
	}

	@Override
	protected String getDebugMessage() {
		return "Refactoring started: ";
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		//Nothing to populate here
	}

	@Override
	protected void initializeFrom(OperationLexer operationLexer) {
		//Nothing to initialize		
	}

}
