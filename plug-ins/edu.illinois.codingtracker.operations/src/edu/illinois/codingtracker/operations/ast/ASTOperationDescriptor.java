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
	}

	public static ASTOperationDescriptor createFrom(OperationLexer operationLexer) {
		return new ASTOperationDescriptor(OperationKind.values()[operationLexer.readInt()], operationLexer.readBoolean(),
											operationLexer.readBoolean());
	}

	public void appendContent(StringBuffer sb) {
		sb.append("Operation kind: " + operationKind + "\n");
		sb.append("Is commenting or uncommenting: " + isCommentingOrUncommenting + "\n");
		sb.append("Is undoing: " + isUndoing + "\n");
	}

}
