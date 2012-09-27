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


	public InferredUnknownTransformationOperation() {
		super();
	}

	public InferredUnknownTransformationOperation(long transformationKindID, long transformationID, long timestamp) {
		super(timestamp);
		this.transformationKindID= transformationKindID;
		this.transformationID= transformationID;
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

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		textChunk.append(transformationKindID);
		textChunk.append(transformationID);
	}

	@Override
	protected void initializeFrom(OperationLexer operationLexer) {
		transformationKindID= operationLexer.readLong();
		transformationID= operationLexer.readLong();
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
		sb.append(super.toString());
		return sb.toString();
	}

}
