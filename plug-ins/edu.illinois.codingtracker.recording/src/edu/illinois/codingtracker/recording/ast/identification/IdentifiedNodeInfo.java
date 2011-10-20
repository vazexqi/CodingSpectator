/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.recording.ast.identification;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import edu.illinois.codingtracker.recording.ast.helpers.ASTHelper;

/**
 * 
 * @author Stas Negara
 * 
 */
public class IdentifiedNodeInfo {

	private final ASTNode identifiedNode;

	private final MethodDeclaration containingMethod;

	private final long containingMethodID;


	public IdentifiedNodeInfo(String filePath, ASTNode identifiedNode) {
		this.identifiedNode= identifiedNode;
		if (identifiedNode instanceof MethodDeclaration) {
			containingMethod= null;
			containingMethodID= -1;
		} else {
			containingMethod= ASTHelper.getContainingMethod(identifiedNode);
			if (containingMethod != null) {
				containingMethodID= ASTNodesIdentifier.getPersistentNodeID(filePath, containingMethod);
			} else {
				containingMethodID= -1;
			}
		}
	}

	public ASTNode getIdentifiedNode() {
		return identifiedNode;
	}

	public MethodDeclaration getContainingMethod(boolean shouldGetOriginal) {
		if (shouldGetOriginal) {
			return containingMethod;
		}
		if (containingMethodID != -1) {
			return (MethodDeclaration)ASTNodesIdentifier.getIdentifiedNode(containingMethodID);
		}
		return null;
	}
}
