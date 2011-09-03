/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.ast;

import org.eclipse.jdt.core.dom.ASTNode;

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

	public static enum OperationKind {
		ADD, DELETE, CHANGE
	};

	private OperationKind operationKind;

	private String nodeType;

	//The following 4 fields are mainly for the debugging purposes.
	private String nodeText;

	private String newNodeText; //Is present only for CHANGE operations.

	private int nodeOffset;

	private int nodeLength;

	private long nodeID;

	private long methodID;

	private String fullMethodName; //Is present only for operations on AST nodes that are methods.

	public ASTOperation() {
		super();
	}

	public ASTOperation(OperationKind operationKind, ASTNode astNode, String newNodeText, long nodeID, long methodID, String fullMethodName, long timestamp) {
		super(timestamp);
		this.operationKind= operationKind;
		nodeType= astNode.getClass().getSimpleName();
		nodeText= astNode.toString();
		this.newNodeText= newNodeText;
		nodeOffset= astNode.getStartPosition();
		nodeLength= astNode.getLength();
		this.nodeID= nodeID;
		this.methodID= methodID;
		this.fullMethodName= fullMethodName;
	}

	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.AST_OPERATION_SYMBOL;
	}

	@Override
	public String getDescription() {
		return "AST operation";
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		int kindOrdinal= operationKind.ordinal();
		textChunk.append(kindOrdinal);
		textChunk.append(nodeType);
		textChunk.append(nodeText);
		textChunk.append(newNodeText);
		textChunk.append(nodeOffset);
		textChunk.append(nodeLength);
		textChunk.append(nodeID);
		textChunk.append(methodID);
		textChunk.append(fullMethodName);
	}

	@Override
	protected void initializeFrom(OperationLexer operationLexer) {
		operationKind= OperationKind.values()[operationLexer.readInt()];
		nodeType= operationLexer.readString();
		nodeText= operationLexer.readString();
		newNodeText= operationLexer.readString();
		nodeOffset= operationLexer.readInt();
		nodeLength= operationLexer.readInt();
		nodeID= operationLexer.readLong();
		methodID= operationLexer.readLong();
		fullMethodName= operationLexer.readString();
	}

	@Override
	public void replay() {
		//do nothing
	}

	@Override
	public String toString() {
		StringBuffer sb= new StringBuffer();
		sb.append("Operation kind: " + operationKind + "\n");
		sb.append("Node type: " + nodeType + "\n");
		sb.append("Node text: " + nodeText + "\n");
		if (!newNodeText.isEmpty()) {
			sb.append("New node text: " + newNodeText + "\n");
		}
		sb.append("Node offset: " + nodeOffset + "\n");
		sb.append("Node length: " + nodeLength + "\n");
		sb.append("Node ID: " + nodeID + "\n");
		sb.append("Method ID: " + methodID + "\n");
		if (!fullMethodName.isEmpty()) {
			sb.append("Fully qualified method name: " + fullMethodName + "\n");
		}
		sb.append(super.toString());
		return sb.toString();
	}

}
