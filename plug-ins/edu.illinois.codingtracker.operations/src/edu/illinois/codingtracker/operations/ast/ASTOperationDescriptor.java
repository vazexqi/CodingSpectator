/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.ast;



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

}
