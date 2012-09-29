/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.transformation;

import org.eclipse.jdt.core.dom.ASTNode;

import edu.illinois.codingtracker.operations.ast.ASTOperationDescriptor.OperationKind;
import edu.illinois.codingtracker.operations.ast.UnknownTransformationDescriptor;



/**
 * This class represents a particular pattern of an unknown transformation.
 * 
 * @author Stas Negara
 * 
 */
public class UnknownTransformationPattern {

	private final UnknownTransformationDescriptor transformationDescriptor;


	public UnknownTransformationPattern(OperationKind operationKind, ASTNode affectedNode) {
		transformationDescriptor= new UnknownTransformationDescriptor(operationKind, getNodeType(affectedNode), affectedNode.toString(), getNodeType(affectedNode.getParent()));
	}

	public UnknownTransformationDescriptor getTransformationDescriptor() {
		return transformationDescriptor;
	}

	private String getNodeType(ASTNode node) {
		if (node == null) {
			return "";
		}
		return node.getClass().getSimpleName();
	}

}
