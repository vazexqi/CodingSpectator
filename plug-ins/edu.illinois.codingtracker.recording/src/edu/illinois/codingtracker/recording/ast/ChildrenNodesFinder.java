/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.recording.ast;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;

/**
 * 
 * @author Stas Negara
 * 
 */
class ChildrenNodesFinder extends ASTVisitor {

	private final Set<ASTNode> childrenNodes= new HashSet<ASTNode>();

	public ChildrenNodesFinder(ASTNode node) {
		super(true);
		node.accept(this);
	}

	public Set<ASTNode> getChildrenNodes() {
		return childrenNodes;
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		childrenNodes.add(node);
		return true;
	}

}
