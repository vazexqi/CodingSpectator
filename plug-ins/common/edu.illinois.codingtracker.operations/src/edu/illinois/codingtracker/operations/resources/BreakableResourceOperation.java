/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.resources;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import edu.illinois.codingtracker.helpers.Configuration;
import edu.illinois.codingtracker.helpers.Debugger;
import edu.illinois.codingtracker.operations.OperationLexer;
import edu.illinois.codingtracker.operations.OperationTextChunk;

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

	public BreakableResourceOperation(IResource resource, boolean success, long timestamp) {
		super(resource, timestamp);
		this.success= success;
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		super.populateTextChunk(textChunk);
		textChunk.append(success);
	}

	@Override
	protected void initializeFrom(OperationLexer operationLexer) {
		super.initializeFrom(operationLexer);
		if (!Configuration.isOldFormat) {
			success= operationLexer.readBoolean();
		} else {
			success= true;
		}
	}

	@Override
	public void replay() throws CoreException {
		if (isReplayedRefactoring) {
			//Do not replay effects of replayed refactorings, since they are replayed as the whole.
			return;
		}
		if (success) {
			replayBreakableResourceOperation();
		} else {
			Debugger.debugWarning("Ignored unsuccessful resource operation:\n" + this);
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
