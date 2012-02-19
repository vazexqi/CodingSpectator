/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.junit;

import edu.illinois.codingtracker.operations.OperationLexer;
import edu.illinois.codingtracker.operations.OperationSymbols;
import edu.illinois.codingtracker.operations.OperationTextChunk;

/**
 * 
 * @author Stas Negara
 * 
 */
public class TestCaseFinishedOperation extends JUnitOperation {

	private String result;

	public TestCaseFinishedOperation() {
		super();
	}

	public TestCaseFinishedOperation(String testRunName, String result) {
		super(testRunName);
		this.result= result;
	}

	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.TEST_CASE_FINISHED_SYMBOL;
	}

	@Override
	public String getDescription() {
		return "Finished test case";
	}

	public boolean hasFailed() {
		//Both "Failure" and "Error" mean that the test has failed.
		return result.equals("Failure") || result.equals("Error");
	}

	public boolean hasPassed() {
		return result.equals("OK");
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		super.populateTextChunk(textChunk);
		textChunk.append(result);
	}

	@Override
	protected void initializeFrom(OperationLexer operationLexer) {
		super.initializeFrom(operationLexer);
		result= operationLexer.readString();
	}

	@Override
	public String toString() {
		StringBuffer sb= new StringBuffer();
		sb.append("Result: " + result + "\n");
		sb.append(super.toString());
		return sb.toString();
	}

}
