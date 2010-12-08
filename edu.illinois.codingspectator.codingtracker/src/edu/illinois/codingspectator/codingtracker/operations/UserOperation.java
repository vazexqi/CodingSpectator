/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations;

import edu.illinois.codingspectator.codingtracker.helpers.Debugger;
import edu.illinois.codingspectator.codingtracker.recording.TextChunk;
import edu.illinois.codingspectator.codingtracker.recording.TextRecorder;

/**
 * 
 * @author Stas Negara
 * 
 * 
 */
public abstract class UserOperation {

	private final String operationSymbol;

	private final String debugMessage;

	private final long timestamp;

	public UserOperation(String operationSymbol, String debugMessage) {
		this.operationSymbol= operationSymbol;
		this.debugMessage= debugMessage;
		timestamp= System.currentTimeMillis();
	}

	public void serialize(TextRecorder textRecorder) {
		TextChunk textChunk= new TextChunk(operationSymbol);
		populateTextChunk(textChunk);
		textChunk.append(timestamp);
		Debugger.debugTextChunk(debugMessage, textChunk);
		textRecorder.record(textChunk);
	}

	protected abstract void populateTextChunk(TextChunk textChunk);

}
