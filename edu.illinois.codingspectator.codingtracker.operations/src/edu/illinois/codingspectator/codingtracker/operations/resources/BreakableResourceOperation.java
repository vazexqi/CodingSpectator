/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.resources;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import edu.illinois.codingspectator.codingtracker.helpers.Debugger;
import edu.illinois.codingspectator.codingtracker.operations.OperationLexer;
import edu.illinois.codingspectator.codingtracker.operations.OperationTextChunk;

/**
 * 
 * @author Stas Negara
 * 
 */
public abstract class BreakableResourceOperation extends ResourceOperation {

	private boolean success;

	public BreakableResourceOperation() {
		super();
	}

	public BreakableResourceOperation(IResource resource, boolean success) {
		super(resource);
		this.success= success;
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		super.populateTextChunk(textChunk);
		textChunk.append(success ? 1 : 0);
	}

	@Override
	protected void initializeFrom(OperationLexer operationLexer) {
		super.initializeFrom(operationLexer);
		success= Integer.valueOf(operationLexer.getNextLexeme()) == 1 ? true : false;
	}

	@Override
	public void replay() throws CoreException {
		if (success) {
			replayBreakableResourceOperation();
		} else {
			Debugger.debugWarning("Ignored unsuccessful resource operation: " + this);
		}
	}

	@Override
	public String toString() {
		StringBuffer sb= new StringBuffer();
		sb.append("Success: " + success + "\n");
		sb.append(super.toString());
		return sb.toString();
	}

	protected abstract void replayBreakableResourceOperation() throws CoreException;

}
