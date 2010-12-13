/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations;

import edu.illinois.codingspectator.codingtracker.helpers.Debugger;

/**
 * 
 * @author Stas Negara
 * 
 */
public abstract class UserOperation {

	private long timestamp;

	public UserOperation() {
		timestamp= System.currentTimeMillis();
	}

	public UserOperation(long timestamp) {
		this.timestamp= timestamp;
	}

	public OperationTextChunk generateTextualRepresentation() {
		OperationTextChunk textChunk= new OperationTextChunk(getOperationSymbol());
		populateTextChunk(textChunk);
		textChunk.append(timestamp);
		Debugger.debugTextChunk(getDebugMessage(), textChunk);
		return textChunk;
	}

	public void deserialize(OperationLexer operationLexer) {
		assert operationLexer.getCurrentOperationSymbol() == getOperationSymbol();
		initializeFrom(operationLexer);
		timestamp= Long.valueOf(operationLexer.getNextLexeme());
	}

	protected abstract char getOperationSymbol();

	protected abstract String getDebugMessage();

	protected abstract void populateTextChunk(OperationTextChunk textChunk);

	protected abstract void initializeFrom(OperationLexer operationLexer);

}
