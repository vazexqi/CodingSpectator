/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.files.breakable;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import edu.illinois.codingspectator.codingtracker.helpers.Debugger;
import edu.illinois.codingspectator.codingtracker.operations.OperationLexer;
import edu.illinois.codingspectator.codingtracker.operations.OperationTextChunk;
import edu.illinois.codingspectator.codingtracker.operations.files.FileOperation;

/**
 * 
 * @author Stas Negara
 * 
 */
public abstract class BreakableFileOperation extends FileOperation {

	private boolean success;

	public BreakableFileOperation() {
		super();
	}

	public BreakableFileOperation(IFile file, boolean success) {
		super(file);
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
			replayBreakableFileOperation();
		} else {
			Debugger.debugWarning("Ignored unsuccessful file operation: " + this);
		}
	}

	@Override
	public String toString() {
		StringBuffer sb= new StringBuffer();
		sb.append("Success: " + success + "\n");
		sb.append(super.toString());
		return sb.toString();
	}

	protected abstract void replayBreakableFileOperation() throws CoreException;

}
