/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.files;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import edu.illinois.codingspectator.codingtracker.operations.OperationSymbols;

/**
 * 
 * @author Stas Negara
 * 
 */
public class InitiallyCommittedFileOperation extends SnapshotedFileOperation {

	public InitiallyCommittedFileOperation() {
		super();
	}

	public InitiallyCommittedFileOperation(IFile initiallyCommittedFile) {
		super(initiallyCommittedFile);
	}

	@Override
	protected char getOperationSymbol() {
		return OperationSymbols.FILE_INITIALLY_COMMITTED_SYMBOL;
	}

	@Override
	public String getDescription() {
		return "Initially committed file";
	}

	@Override
	public void replay() throws CoreException {
		checkSnapshotMatchesTheExistingFile();
		super.replay();
	}

	@Override
	public boolean isTestReplayRecorded() {
		return false;
	}

}
