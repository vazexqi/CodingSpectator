/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.files.snapshoted;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

/**
 * 
 * @author Stas Negara
 * 
 */
public abstract class CommittedFileOperation extends SnapshotedFileOperation {

	public CommittedFileOperation() {
		super();
	}

	public CommittedFileOperation(IFile committedFile) {
		super(committedFile);
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
