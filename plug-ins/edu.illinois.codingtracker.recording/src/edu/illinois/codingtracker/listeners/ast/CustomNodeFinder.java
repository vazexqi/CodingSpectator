/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.listeners.ast;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;

/**
 * 
 * @author Stas Negara
 * 
 */
class CustomNodeFinder extends ASTVisitor {
	private final int start;

	private final int end;

	private ASTNode coveringNode;

	private final List<ASTNode> coveredNodes= new LinkedList<ASTNode>();

	public CustomNodeFinder(ASTNode rootNode, int offset, int length) {
		super(true);
		start= offset;
		end= offset + length;
		rootNode.accept(this);
	}

	public ASTNode getCoveringNode() {
		return coveringNode;
	}

	public List<ASTNode> getCoveredNodes() {
		return coveredNodes;
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		int nodeStart= node.getStartPosition();
		int nodeEnd= nodeStart + node.getLength();
		if (start > nodeEnd || end < nodeStart) {
			return false;
		}
		if (start >= nodeStart && end <= nodeEnd) {
			coveringNode= node;
		}
		if (start <= nodeStart && end >= nodeEnd) {
			coveredNodes.add(node);
		}
		return true;
	}

}
