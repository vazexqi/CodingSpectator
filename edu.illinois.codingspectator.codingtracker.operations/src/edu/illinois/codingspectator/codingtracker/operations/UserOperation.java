/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations;

import java.util.Date;

import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.ui.IEditorPart;

import edu.illinois.codingspectator.codingtracker.helpers.Debugger;

/**
 * 
 * @author Stas Negara
 * 
 */
public abstract class UserOperation {

	public static final boolean isInTestMode= System.getenv("TEST_MODE") != null;

	public static final boolean isOldFormat= System.getenv("OLD_CODINGTRACKER_FORMAT") != null;

	//Made public to be able to assign when the replayer is loaded/reset
	public static boolean isRefactoring= false;

	protected static IEditorPart currentEditor= null;

	private long timestamp;

	public UserOperation() {
		timestamp= System.currentTimeMillis();
	}

	public UserOperation(long timestamp) {
		this.timestamp= timestamp;
	}

	public OperationTextChunk generateSerializationText() {
		OperationTextChunk textChunk= new OperationTextChunk(getOperationSymbol());
		populateTextChunk(textChunk);
		textChunk.append(timestamp);
		Debugger.debugTextChunk(getDescription() + ": ", textChunk);
		return textChunk;
	}

	public void deserialize(OperationLexer operationLexer) {
		if (operationLexer.getCurrentOperationSymbol() != getOperationSymbol()) {
			throw new AssertionFailedException("Mismatch between lexer current operation symbol and the actual operation");
		}
		initializeFrom(operationLexer);
		timestamp= operationLexer.readLong();
	}

	@Override
	public String toString() {
		return "Timestamp: " + timestamp;
	}

	public long getTime() {
		return timestamp;
	}

	public Date getDate() {
		return new Date(timestamp);
	}

	public boolean isTestReplayRecorded() {
		return true;
	}

	protected abstract char getOperationSymbol();

	public abstract String getDescription();

	protected abstract void populateTextChunk(OperationTextChunk textChunk);

	protected abstract void initializeFrom(OperationLexer operationLexer);

	public abstract void replay() throws Exception;

}
