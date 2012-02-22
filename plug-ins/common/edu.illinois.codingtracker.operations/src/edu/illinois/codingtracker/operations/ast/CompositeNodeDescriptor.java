/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.ast;

import java.util.HashSet;
import java.util.Set;

import edu.illinois.codingtracker.operations.OperationLexer;
import edu.illinois.codingtracker.operations.OperationTextChunk;


/**
 * 
 * @author Stas Negara
 * 
 */
public class CompositeNodeDescriptor {

	private final ASTNodeDescriptor nodeDescriptor;

	private final ASTMethodDescriptor containingMethodDescriptor;

	//IDs of the nodes that are in the same "cluster" as the affected node, e.g. the affected node's children.
	private final Set<Long> clusterNodeIDs;


	public CompositeNodeDescriptor(ASTNodeDescriptor nodeDescriptor, ASTMethodDescriptor containingMethodDescriptor, Set<Long> clusterNodeIDs) {
		this.nodeDescriptor= nodeDescriptor;
		this.containingMethodDescriptor= containingMethodDescriptor;
		this.clusterNodeIDs= clusterNodeIDs;
	}

	public long getNodeID() {
		return nodeDescriptor.getNodeID();
	}

	public String getPositionalID() {
		return nodeDescriptor.getPositionalID();
	}

	public String getNodeType() {
		return nodeDescriptor.getNodeType();
	}

	public String getNodeText() {
		return nodeDescriptor.getNodeText();
	}

	public String getNodeNewText() {
		return nodeDescriptor.getNodeNewText();
	}

	public int getNodeOffset() {
		return nodeDescriptor.getNodeOffset();
	}

	public int getNodeLength() {
		return nodeDescriptor.getNodeLength();
	}

	public long getMethodID() {
		return containingMethodDescriptor.getMethodID();
	}

	public String getMethodFullName() {
		return containingMethodDescriptor.getMethodFullName();
	}

	public int getMethodLinesCount() {
		return containingMethodDescriptor.getMethodLinesCount();
	}

	public int getMethodCyclomaticComplexity() {
		return containingMethodDescriptor.getMethodCyclomaticComplexity();
	}

	public Set<Long> getClusterNodeIDs() {
		return clusterNodeIDs;
	}

	public void populateTextChunk(OperationTextChunk textChunk) {
		nodeDescriptor.populateTextChunk(textChunk);
		containingMethodDescriptor.populateTextChunk(textChunk);
		textChunk.append(clusterNodeIDs.size());
		for (long clusterNodID : clusterNodeIDs) {
			textChunk.append(clusterNodID);
		}
	}

	public static CompositeNodeDescriptor createFrom(OperationLexer operationLexer) {
		ASTNodeDescriptor nodeDescriptor= ASTNodeDescriptor.createFrom(operationLexer);
		ASTMethodDescriptor methodDescriptor= ASTMethodDescriptor.createFrom(operationLexer);
		int clusterNodeIDsCount= operationLexer.readInt();
		Set<Long> clusterNodeIDs= new HashSet<Long>(clusterNodeIDsCount);
		for (int counter= 0; counter < clusterNodeIDsCount; counter++) {
			clusterNodeIDs.add(operationLexer.readLong());
		}
		return new CompositeNodeDescriptor(nodeDescriptor, methodDescriptor, clusterNodeIDs);
	}

	public void appendContent(StringBuffer sb) {
		nodeDescriptor.appendContent(sb);
		containingMethodDescriptor.appendContent(sb);
		sb.append("Cluster nodes count: " + clusterNodeIDs.size() + "\n");
		for (long clusterNodeID : clusterNodeIDs) {
			sb.append("Cluster node ID: " + clusterNodeID + "\n");
		}
	}

}
