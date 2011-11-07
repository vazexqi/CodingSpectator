/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.ast;


/**
 * 
 * @author Stas Negara
 * 
 */
public class ASTNodeDescriptor {

	private final long nodeID;

	private final String nodeType;

	private final String nodeText;

	private final String nodeNewText;

	private final int nodeOffset;

	private final int nodeLength;


	public ASTNodeDescriptor(long nodeID, String nodeType, String nodeText, String nodeNewText, int nodeOffset, int nodeLength) {
		this.nodeID= nodeID;
		this.nodeType= nodeType;
		this.nodeText= nodeText;
		this.nodeNewText= nodeNewText;
		this.nodeOffset= nodeOffset;
		this.nodeLength= nodeLength;
	}

	public long getNodeID() {
		return nodeID;
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

}
