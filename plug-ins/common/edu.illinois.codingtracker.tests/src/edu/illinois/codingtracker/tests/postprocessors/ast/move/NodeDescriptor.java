/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.move;

import edu.illinois.codingtracker.operations.ast.ASTOperation;



/**
 * 
 * @author Stas Negara
 * 
 */
public class NodeDescriptor {

	private final String nodeType;

	private final String nodeText;


	public NodeDescriptor(ASTOperation astOperation, boolean isDeletingChange) {
		nodeType= astOperation.getNodeType();
		if (astOperation.isChange() && !isDeletingChange) {
			nodeText= astOperation.getNodeNewText();
		} else {
			nodeText= astOperation.getNodeText();
		}
	}

	public String getNodeText() {
		return nodeText;
	}

	@Override
	public int hashCode() {
		final int prime= 31;
		int result= 1;
		result= prime * result + ((nodeText == null) ? 0 : nodeText.hashCode());
		result= prime * result + ((nodeType == null) ? 0 : nodeType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NodeDescriptor other= (NodeDescriptor)obj;
		if (nodeText == null) {
			if (other.nodeText != null)
				return false;
		} else if (!nodeText.equals(other.nodeText))
			return false;
		if (nodeType == null) {
			if (other.nodeType != null)
				return false;
		} else if (!nodeType.equals(other.nodeType))
			return false;
		return true;
	}

}
