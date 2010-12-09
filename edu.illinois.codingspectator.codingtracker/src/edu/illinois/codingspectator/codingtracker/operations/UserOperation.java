/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations;

import edu.illinois.codingspectator.codingtracker.helpers.Debugger;
import edu.illinois.codingspectator.codingtracker.recording.TextRecorder;

/**
 * 
 * @author Stas Negara
 * 
 */
public abstract class UserOperation {

	private final long timestamp;

	public UserOperation() {
		timestamp= System.currentTimeMillis();
	}

	public UserOperation(long timestamp) {
		this.timestamp= timestamp;
	}

	public void serialize(TextRecorder textRecorder) {
		OperationTextChunk textChunk= new OperationTextChunk(getOperationSymbol());
		populateTextChunk(textChunk);
		textChunk.append(timestamp);
		Debugger.debugTextChunk(getDebugMessage(), textChunk);
		textRecorder.record(textChunk);
	}

	protected abstract String getOperationSymbol();

	protected abstract void populateTextChunk(OperationTextChunk textChunk);

	protected abstract String getDebugMessage();

}
