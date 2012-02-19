/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.starts;

import edu.illinois.codingtracker.operations.OperationLexer;
import edu.illinois.codingtracker.operations.OperationSymbols;
import edu.illinois.codingtracker.operations.OperationTextChunk;
import edu.illinois.codingtracker.operations.UserOperation;

/**
 * This operation is no longer recorded.
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
	public String getDescription() {
		return "Started refactoring";
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		//Nothing to populate here
	}

	@Override
	protected void initializeFrom(OperationLexer operationLexer) {
		//Nothing to initialize		
	}

	@Override
	public void replay() {
		isReplayedRefactoring= true;
	}

}
