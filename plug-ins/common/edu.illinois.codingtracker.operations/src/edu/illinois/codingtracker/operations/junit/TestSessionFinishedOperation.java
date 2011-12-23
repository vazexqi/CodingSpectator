/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.junit;

import edu.illinois.codingtracker.operations.OperationSymbols;

/**
 * 
 * @author Stas Negara
 * 
 */
public class TestSessionFinishedOperation extends JUnitOperation {

	public TestSessionFinishedOperation() {
		super();
	}

	public TestSessionFinishedOperation(String testRunName) {
		super(testRunName);
	}

	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.TEST_SESSION_FINISHED_SYMBOL;
	}

	@Override
	public String getDescription() {
		return "Finished test session";
	}

}
