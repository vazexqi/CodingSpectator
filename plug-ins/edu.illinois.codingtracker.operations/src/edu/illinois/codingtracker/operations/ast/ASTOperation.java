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
public class ASTOperation extends UserOperation {

	private ASTOperationDescriptor operationDescriptor;

	private CompositeNodeDescriptor affectedNodeDescriptor;


	public ASTOperation() {
		super();
	}

	public ASTOperation(ASTOperationDescriptor operationDescriptor, CompositeNodeDescriptor affectedNodeDescriptor, long timestamp) {
		super(timestamp);
		this.operationDescriptor= operationDescriptor;
		this.affectedNodeDescriptor= affectedNodeDescriptor;
	}

	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.AST_OPERATION_SYMBOL;
	}

	@Override
	public String getDescription() {
		return "AST operation";
	}

	public long getNodeID() {
		return affectedNodeDescriptor.getNodeID();
	}

	public long getMethodID() {
		return affectedNodeDescriptor.getMethodID();
	}

	public String getMethodName() {
		return affectedNodeDescriptor.getMethodFullName();
	}

	public int getMethodLinesCount() {
		return affectedNodeDescriptor.getMethodLinesCount();
	}

	public int getMethodCyclomaticComplexity() {
		return affectedNodeDescriptor.getMethodCyclomaticComplexity();
	}

	public boolean isCommentingOrUncommenting() {
		return operationDescriptor.isCommentingOrUncommenting();
	}

	public boolean isUndoing() {
		return operationDescriptor.isUndoing();
	}

	public boolean isAdd() {
		return operationDescriptor.isAdd();
	}

	public boolean isChange() {
		return operationDescriptor.isChange();
	}

	public boolean isDelete() {
		return operationDescriptor.isDelete();
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		operationDescriptor.populateTextChunk(textChunk);
		affectedNodeDescriptor.populateTextChunk(textChunk);
	}

	@Override
	protected void initializeFrom(OperationLexer operationLexer) {
		operationDescriptor= ASTOperationDescriptor.createFrom(operationLexer);
		affectedNodeDescriptor= CompositeNodeDescriptor.createFrom(operationLexer);
	}

	@Override
	public void replay() {
		//do nothing
	}

	@Override
	public String toString() {
		StringBuffer sb= new StringBuffer();
		operationDescriptor.appendContent(sb);
		affectedNodeDescriptor.appendContent(sb);
		sb.append(super.toString());
		return sb.toString();
	}

}
