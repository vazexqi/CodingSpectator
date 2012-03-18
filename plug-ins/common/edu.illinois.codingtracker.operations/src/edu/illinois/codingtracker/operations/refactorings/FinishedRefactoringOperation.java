/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.refactorings;

import edu.illinois.codingtracker.operations.OperationLexer;
import edu.illinois.codingtracker.operations.OperationSymbols;
import edu.illinois.codingtracker.operations.OperationTextChunk;
import edu.illinois.codingtracker.operations.UserOperation;

/**
 * 
 * @author Stas Negara
 * 
 */
public class FinishedRefactoringOperation extends UserOperation {

	private boolean success;

	private boolean isTooSimple; //Optional field, assigned during refactoring inference.


	public FinishedRefactoringOperation() {
		super();
	}

	public FinishedRefactoringOperation(boolean success) {
		super();
		this.success= success;
	}

	/**
	 * This constructor should be used only for the conversion from an older refactoring format to
	 * this newer format.
	 * 
	 * @param success
	 * @param timestamp
	 */
	public FinishedRefactoringOperation(boolean success, long timestamp) {
		super(timestamp);
		this.success= success;
	}

	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.REFACTORING_FINISHED_SYMBOL;
	}

	@Override
	public String getDescription() {
		return "Finished refactoring";
	}

	public boolean getSuccess() {
		return success;
	}

	public boolean isTooSimple() {
		return isTooSimple;
	}

	public void setTooSimple(boolean isTooSimple) {
		this.isTooSimple= isTooSimple;
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		textChunk.append(success);
		textChunk.append(isTooSimple);
	}

	@Override
	protected void initializeFrom(OperationLexer operationLexer) {
		success= operationLexer.readBoolean();
		isTooSimple= operationLexer.readOptionalBoolean();
	}

	@Override
	public void replay() {
		isReplayedRefactoring= false;
	}

	@Override
	public String toString() {
		StringBuffer sb= new StringBuffer();
		sb.append("Success: " + success + "\n");
		sb.append("Is too simple: " + isTooSimple + "\n");
		sb.append(super.toString());
		return sb.toString();
	}

}
