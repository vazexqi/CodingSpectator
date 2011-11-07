/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.ast;


/**
 * 
 * @author Stas Negara
 * 
 */
public class CompositeNodeDescriptor {

	private final ASTNodeDescriptor nodeDescriptor;

	private final ASTMethodDescriptor containingMethodDescriptor;


	public CompositeNodeDescriptor(ASTNodeDescriptor nodeDescriptor, ASTMethodDescriptor containingMethodDescriptor) {
		this.nodeDescriptor= nodeDescriptor;
		this.containingMethodDescriptor= containingMethodDescriptor;
	}

	public long getNodeID() {
		return nodeDescriptor.getNodeID();
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

}
