/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.ast;

import edu.illinois.codingtracker.operations.OperationLexer;
import edu.illinois.codingtracker.operations.OperationTextChunk;



/**
 * 
 * @author Stas Negara
 * 
 */
public class ASTOperationDescriptor {

	public static enum OperationKind {
		ADD, DELETE, CHANGE
	};

	private final OperationKind operationKind;

	private final boolean isCommentingOrUncommenting;

	private final boolean isUndoing;

	//Not final since these fields are assigned while inferring "move", which is done in a separate subsequent pass.

	private long moveID= -1;

	private boolean isFirstMoved= false;

	private boolean isLastMoved= false;


	public ASTOperationDescriptor(OperationKind operationKind, boolean isCommentingOrUncommenting, boolean isUndoing) {
		this.operationKind= operationKind;
		this.isCommentingOrUncommenting= isCommentingOrUncommenting;
		this.isUndoing= isUndoing;
	}

	public OperationKind getOperationKind() {
		return operationKind;
	}

	public boolean isCommentingOrUncommenting() {
		return isCommentingOrUncommenting;
	}

	public boolean isUndoing() {
		return isUndoing;
	}


	public long getMoveID() {
		return moveID;
	}

	public void setMoveID(long moveID) {
		this.moveID= moveID;
	}

	public boolean isFirstMoved() {
		return isFirstMoved;
	}

	public void setFirstMoved(boolean isFirstMoved) {
		this.isFirstMoved= isFirstMoved;
	}

	public boolean isLastMoved() {
		return isLastMoved;
	}

	public void setLastMoved(boolean isLastMoved) {
		this.isLastMoved= isLastMoved;
	}

	public boolean isAdd() {
		return operationKind == OperationKind.ADD;
	}

	public boolean isChange() {
		return operationKind == OperationKind.CHANGE;
	}

	public boolean isDelete() {
		return operationKind == OperationKind.DELETE;
	}

	public void populateTextChunk(OperationTextChunk textChunk) {
		textChunk.append(operationKind.ordinal());
		textChunk.append(isCommentingOrUncommenting);
		textChunk.append(isUndoing);
		textChunk.append(moveID);
		textChunk.append(isFirstMoved);
		textChunk.append(isLastMoved);
	}

	public static ASTOperationDescriptor createFrom(OperationLexer operationLexer) {
		ASTOperationDescriptor operationDescriptor= new ASTOperationDescriptor(OperationKind.values()[operationLexer.readInt()], operationLexer.readBoolean(),
																				operationLexer.readBoolean());
		operationDescriptor.setMoveID(operationLexer.readLong());
		operationDescriptor.setFirstMoved(operationLexer.readBoolean());
		operationDescriptor.setLastMoved(operationLexer.readBoolean());
		return operationDescriptor;
	}

	public void appendContent(StringBuffer sb) {
		sb.append("Operation kind: " + operationKind + "\n");
		sb.append("Is commenting or uncommenting: " + isCommentingOrUncommenting + "\n");
		sb.append("Is undoing: " + isUndoing + "\n");
		sb.append("Move ID: " + moveID + "\n");
		sb.append("Is first moved: " + isFirstMoved + "\n");
		sb.append("Is last moved: " + isLastMoved + "\n");
	}

}
