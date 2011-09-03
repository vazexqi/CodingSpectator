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
class PersistentNodesFinder extends ASTVisitor {

	private final List<ASTNode> excludedNodes= new LinkedList<ASTNode>();

	private final List<ASTNode> persistentNodes= new LinkedList<ASTNode>();

	public PersistentNodesFinder(ASTNode rootNode, List<ASTNode> excludedNodes) {
		super(true);
		this.excludedNodes.addAll(excludedNodes);
		rootNode.accept(this);
	}

	public List<ASTNode> getPersistentNodes() {
		return persistentNodes;
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		if (!excludedNodes.contains(node)) {
			persistentNodes.add(node);
		}
		return true;
	}

}
