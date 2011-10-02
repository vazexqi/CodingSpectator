/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.recording.ast;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;

/**
 * 
 * @author Stas Negara
 * 
 */
class AffectedNodesFinder extends ASTVisitor {
	private final int start;

	private final int end;

	private ASTNode rootNode;

	private ASTNode coveringNode;

	//Note that coveringNode sometimes does NOT make part of affectedNodes, 
	//e.g. in cases when getNodeEnd(rootNode) == start == end.
	private final List<ASTNode> affectedNodes= new LinkedList<ASTNode>();


	public AffectedNodesFinder(ASTNode rootNode, int offset, int length) {
		super(true);
		start= offset;
		end= offset + length;
		this.rootNode= rootNode;
		collectAffectedNodes();
	}

	public ASTNode getRootNode() {
		return rootNode;
	}

	public ASTNode getCoveringNode() {
		return coveringNode;
	}

	public List<ASTNode> getAffectedNodes() {
		return affectedNodes;
	}

	private void collectAffectedNodes() {
		//First, determine the covering node.
		rootNode.accept(this);

		ChildrenNodesFinder childrenNodesFinder= new ChildrenNodesFinder(coveringNode);
		for (ASTNode childNode : childrenNodesFinder.getChildrenNodes()) {
			if (!isOutlier(childNode)) {
				affectedNodes.add(childNode);
			}
		}
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		//[start, end) is intersected with [nodeStart, nodeEnd)
		if (start >= getNodeStart(node) && end <= getNodeEnd(node)) {
			coveringNode= node;
			return true;
		}
		return false;
	}

	private boolean isOutlier(ASTNode node) {
		//[start, end) is intersected with [nodeStart, nodeEnd)
		if (start >= getNodeEnd(node) || end <= getNodeStart(node)) {
			return true;
		}
		return false;
	}

	private int getNodeStart(ASTNode node) {
		return node.getStartPosition();
	}

	private int getNodeEnd(ASTNode node) {
		return node.getStartPosition() + node.getLength();
	}

}
