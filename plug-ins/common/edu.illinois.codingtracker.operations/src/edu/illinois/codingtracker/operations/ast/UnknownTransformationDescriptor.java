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

	private final String affectedNodeContent; //This field is not considered in methods hashCode and equals. 

	private final String abstractedNodeContent;

	// private final String containerNodeType; //Should we care about the parent node's type?


	public UnknownTransformationDescriptor(OperationKind operationKind, String affectedNodeType, String affectedNodeContent, String abstractedNodeContent) {
		this.operationKind= operationKind;
		this.affectedNodeType= affectedNodeType;
		this.affectedNodeContent= affectedNodeContent;
		this.abstractedNodeContent= abstractedNodeContent;
	}

	public void populateTextChunk(OperationTextChunk textChunk) {
		textChunk.append(operationKind.ordinal());
		textChunk.append(affectedNodeType);
		textChunk.append(affectedNodeContent);
		textChunk.append(abstractedNodeContent);
	}

	public static UnknownTransformationDescriptor createFrom(OperationLexer operationLexer) {
		return new UnknownTransformationDescriptor(OperationKind.values()[operationLexer.readInt()], operationLexer.readString(), operationLexer.readString(),
													operationLexer.readString());
	}

	public void appendContent(StringBuffer sb) {
		sb.append("Operation kind: " + operationKind + "\n");
		sb.append("Affected node type: " + affectedNodeType + "\n");
		sb.append("Affected node content: " + affectedNodeContent + "\n");
		sb.append("Abstracted node content: " + abstractedNodeContent + "\n");
	}

	public OperationKind getOperationKind() {
		return operationKind;
	}

	public String getAffectedNodeType() {
		return affectedNodeType;
	}

	public String getAffectedNodeContent() {
		return affectedNodeContent;
	}

	public String getAbstractedNodeContent() {
		return abstractedNodeContent;
	}

	@Override
	public int hashCode() {
		final int prime= 11;
		int result= 1;
		result= prime * result + ((abstractedNodeContent == null) ? 0 : abstractedNodeContent.hashCode());
		result= prime * result + ((affectedNodeType == null) ? 0 : affectedNodeType.hashCode());
		result= prime * result + ((operationKind == null) ? 0 : operationKind.hashCode());
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
		UnknownTransformationDescriptor other= (UnknownTransformationDescriptor)obj;
		if (abstractedNodeContent == null) {
			if (other.abstractedNodeContent != null)
				return false;
		} else if (!abstractedNodeContent.equals(other.abstractedNodeContent))
			return false;
		if (affectedNodeType == null) {
			if (other.affectedNodeType != null)
				return false;
		} else if (!affectedNodeType.equals(other.affectedNodeType))
			return false;
		if (operationKind != other.operationKind)
			return false;
		return true;
	}

}
