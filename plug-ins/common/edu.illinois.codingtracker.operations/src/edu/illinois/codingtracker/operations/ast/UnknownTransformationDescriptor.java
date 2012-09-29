/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.ast;

import edu.illinois.codingtracker.operations.OperationLexer;
import edu.illinois.codingtracker.operations.OperationTextChunk;
import edu.illinois.codingtracker.operations.ast.ASTOperationDescriptor.OperationKind;


/**
 * 
 * @author Stas Negara
 * 
 */
public class UnknownTransformationDescriptor {

	private final OperationKind operationKind;

	private final String affectedNodeType;

	private final String affectedNodeContent;

	private final String containerNodeType;


	public UnknownTransformationDescriptor(OperationKind operationKind, String affectedNodeType, String affectedNodeContent, String containerNodeType) {
		this.operationKind= operationKind;
		this.affectedNodeType= affectedNodeType;
		this.affectedNodeContent= affectedNodeContent;
		this.containerNodeType= containerNodeType;
	}

	public void populateTextChunk(OperationTextChunk textChunk) {
		textChunk.append(operationKind.ordinal());
		textChunk.append(affectedNodeType);
		textChunk.append(affectedNodeContent);
		textChunk.append(containerNodeType);
	}

	public static UnknownTransformationDescriptor createFrom(OperationLexer operationLexer) {
		return new UnknownTransformationDescriptor(OperationKind.values()[operationLexer.readInt()], operationLexer.readString(), operationLexer.readString(), operationLexer.readString());
	}

	public void appendContent(StringBuffer sb) {
		sb.append("Operation kind: " + operationKind + "\n");
		sb.append("Affected node type: " + affectedNodeType + "\n");
		sb.append("Affected node content: " + affectedNodeContent + "\n");
		sb.append("Container node type: " + containerNodeType + "\n");
	}

}
