/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.ast;

import edu.illinois.codingtracker.operations.OperationLexer;
import edu.illinois.codingtracker.operations.OperationSymbols;
import edu.illinois.codingtracker.operations.OperationTextChunk;
import edu.illinois.codingtracker.operations.UserOperation;

/**
 * 
 * @author Stas Negara
 * 
 */
public class InferredUnknownTransformationOperation extends UserOperation {

	private long transformationKindID;

	private long transformationID;

	private UnknownTransformationDescriptor transformationDescriptor;


	public InferredUnknownTransformationOperation() {
		super();
	}

	public InferredUnknownTransformationOperation(long transformationKindID, long transformationID, UnknownTransformationDescriptor transformationDescriptor, long timestamp) {
		super(timestamp);
		this.transformationKindID= transformationKindID;
		this.transformationID= transformationID;
		this.transformationDescriptor= transformationDescriptor;
	}


	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.INFERRED_UNKNOWN_TRANSFORMATION_OPERATION_SYMBOL;
	}

	@Override
	public String getDescription() {
		return "Inferred unknown transformation";
	}

	public long getTransformationKindID() {
		return transformationKindID;
	}

	public long getTransformationID() {
		return transformationID;
	}

	public UnknownTransformationDescriptor getDescriptor() {
		return transformationDescriptor;
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		textChunk.append(transformationKindID);
		textChunk.append(transformationID);
		transformationDescriptor.populateTextChunk(textChunk);
	}

	@Override
	protected void initializeFrom(OperationLexer operationLexer) {
		transformationKindID= operationLexer.readLong();
		transformationID= operationLexer.readLong();
		transformationDescriptor= UnknownTransformationDescriptor.createFrom(operationLexer);
	}

	@Override
	public void replay() {
		//do nothing
	}

	@Override
	public String toString() {
		StringBuffer sb= new StringBuffer();
		sb.append("Transformation kind ID: " + transformationKindID + "\n");
		sb.append("Transformation ID: " + transformationID + "\n");
		transformationDescriptor.appendContent(sb);
		sb.append(super.toString());
		return sb.toString();
	}

}
