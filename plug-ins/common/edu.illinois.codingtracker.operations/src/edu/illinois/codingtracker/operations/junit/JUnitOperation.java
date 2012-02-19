/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.junit;

import edu.illinois.codingtracker.operations.OperationLexer;
import edu.illinois.codingtracker.operations.OperationTextChunk;
import edu.illinois.codingtracker.operations.UserOperation;

/**
 * 
 * @author Stas Negara
 * 
 */
public abstract class JUnitOperation extends UserOperation {

	private String testRunName;

	public JUnitOperation() {
		super();
	}

	public JUnitOperation(String testRunName) {
		super();
		this.testRunName= testRunName;
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		textChunk.append(testRunName);
	}

	@Override
	protected void initializeFrom(OperationLexer operationLexer) {
		testRunName= operationLexer.readString();
	}

	@Override
	public void replay() {
		//do nothing
	}

	@Override
	public String toString() {
		StringBuffer sb= new StringBuffer();
		sb.append("TestRunName: " + testRunName + "\n");
		sb.append(super.toString());
		return sb.toString();
	}

}
