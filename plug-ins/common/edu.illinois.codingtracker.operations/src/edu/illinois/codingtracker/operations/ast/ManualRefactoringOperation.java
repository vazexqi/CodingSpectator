/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.ast;

import edu.illinois.codingtracker.operations.OperationLexer;
import edu.illinois.codingtracker.operations.OperationSymbols;
import edu.illinois.codingtracker.operations.OperationTextChunk;
import edu.illinois.codingtracker.operations.UserOperation;

/**
 * 
 * @author Stas Negara
 * 
 */
public class ManualRefactoringOperation extends UserOperation {

	public static enum RefactoringKind {
		EXTRACT_LOCAL_VARIABLE, EXTRACT_LOCAL_CONSTANT, INLINE_LOCAL_VARIABLE, INLINE_LOCAL_CONSTANT, RENAME_LOCAL_VARIABLE
	};

	private RefactoringKind refactoringKind;

	private long refactoringID;


	public ManualRefactoringOperation() {
		super();
	}

	public ManualRefactoringOperation(RefactoringKind refactoringKind, long refactoringID, long timestamp) {
		super(timestamp);
		this.refactoringKind= refactoringKind;
		this.refactoringID= refactoringID;
	}


	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.MANUAL_REFACTORING_OPERATION_SYMBOL;
	}

	@Override
	public String getDescription() {
		return "Inferred manual refactoring";
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		textChunk.append(refactoringKind.ordinal());
		textChunk.append(refactoringID);
	}

	@Override
	protected void initializeFrom(OperationLexer operationLexer) {
		refactoringKind= RefactoringKind.values()[operationLexer.readInt()];
		refactoringID= operationLexer.readLong();
	}

	@Override
	public void replay() {
		//do nothing
	}

	@Override
	public String toString() {
		StringBuffer sb= new StringBuffer();
		sb.append("Refactoring kind: " + refactoringKind + "\n");
		sb.append("Refactoring ID: " + refactoringID + "\n");
		sb.append(super.toString());
		return sb.toString();
	}

}
