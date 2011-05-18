/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.conflicteditors;

import edu.illinois.codingtracker.operations.OperationLexer;
import edu.illinois.codingtracker.operations.OperationTextChunk;
import edu.illinois.codingtracker.operations.UserOperation;

/**
 * 
 * @author Stas Negara
 * 
 */
public abstract class ConflictEditorOperation extends UserOperation {

	protected String editorID;

	public ConflictEditorOperation() {
		super();
	}

	public ConflictEditorOperation(String editorID) {
		super();
		this.editorID= editorID;
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		textChunk.append(editorID);
	}

	@Override
	protected void initializeFrom(OperationLexer operationLexer) {
		editorID= operationLexer.readString();
	}

	@Override
	public String toString() {
		StringBuffer sb= new StringBuffer();
		sb.append("Editor ID: " + editorID + "\n");
		sb.append(super.toString());
		return sb.toString();
	}

}
