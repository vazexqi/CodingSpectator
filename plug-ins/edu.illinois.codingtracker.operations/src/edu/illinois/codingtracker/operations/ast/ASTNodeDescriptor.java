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
public class ASTNodeDescriptor {

	private final long nodeID; //persistent ID

	private final String positionalID;

	private final String nodeType;

	private final String nodeText;

	private final String nodeNewText;

	private final int nodeOffset;

	private final int nodeLength;


	public ASTNodeDescriptor(long nodeID, String positionalID, String nodeType, String nodeText, String nodeNewText, int nodeOffset, int nodeLength) {
		this.nodeID= nodeID;
		this.positionalID= positionalID;
		this.nodeType= nodeType;
		this.nodeText= nodeText;
		this.nodeNewText= nodeNewText;
		this.nodeOffset= nodeOffset;
		this.nodeLength= nodeLength;
	}

	public long getNodeID() {
		return nodeID;
	}

	public String getPositionalID() {
		return positionalID;
	}

	public String getNodeType() {
		return nodeType;
	}

	public String getNodeText() {
		return nodeText;
	}

	public String getNodeNewText() {
		return nodeNewText;
	}

	public int getNodeOffset() {
		return nodeOffset;
	}

	public int getNodeLength() {
		return nodeLength;
	}

	public void populateTextChunk(OperationTextChunk textChunk) {
		textChunk.append(nodeID);
		textChunk.append(positionalID);
		textChunk.append(nodeType);
		textChunk.append(nodeText);
		textChunk.append(nodeNewText);
		textChunk.append(nodeOffset);
		textChunk.append(nodeLength);
	}

	public static ASTNodeDescriptor createFrom(OperationLexer operationLexer) {
		return new ASTNodeDescriptor(operationLexer.readLong(), operationLexer.readString(), operationLexer.readString(), operationLexer.readString(),
										operationLexer.readString(), operationLexer.readInt(), operationLexer.readInt());
	}

	public void appendContent(StringBuffer sb) {
		sb.append("Node ID: " + nodeID + "\n");
		sb.append("Positional ID: " + positionalID + "\n");
		sb.append("Node type: " + nodeType + "\n");
		sb.append("Node text: " + nodeText + "\n");
		if (!nodeNewText.isEmpty()) {
			sb.append("New node text: " + nodeNewText + "\n");
		}
		sb.append("Node offset: " + nodeOffset + "\n");
		sb.append("Node length: " + nodeLength + "\n");
	}

}
