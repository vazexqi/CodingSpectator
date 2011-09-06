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
class ChildrenNodesFinder extends ASTVisitor {

	private final List<ASTNode> childrenNodes= new LinkedList<ASTNode>();

	public ChildrenNodesFinder(ASTNode node) {
		super(true);
		node.accept(this);
	}

	public List<ASTNode> getChildrenNodes() {
		return childrenNodes;
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		childrenNodes.add(node);
		return true;
	}

}
