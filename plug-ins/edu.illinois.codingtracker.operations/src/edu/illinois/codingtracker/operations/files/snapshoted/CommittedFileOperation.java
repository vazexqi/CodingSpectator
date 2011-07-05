/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.files.snapshoted;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import edu.illinois.codingtracker.operations.OperationLexer;
import edu.illinois.codingtracker.operations.OperationTextChunk;

/**
 * 
 * @author Stas Negara
 * 
 */
public abstract class CommittedFileOperation extends SnapshotedFileOperation {

	private String revision;

	private String committedRevision;


	public CommittedFileOperation() {
		super();
	}

	public CommittedFileOperation(IFile committedFile, String revision, String committedRevision) {
		super(committedFile);
		this.revision= revision;
		this.committedRevision= committedRevision;
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		super.populateTextChunk(textChunk);
		textChunk.append(revision);
		textChunk.append(committedRevision);
	}

	@Override
	protected void initializeFrom(OperationLexer operationLexer) {
		super.initializeFrom(operationLexer);
		if (!isOldFormat) {
			revision= operationLexer.readString();
			committedRevision= operationLexer.readString();
		} else {
			revision= "";
			committedRevision= "";
		}
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

	@Override
	public String toString() {
		StringBuffer sb= new StringBuffer();
		sb.append("Revision " + revision + "\n");
		sb.append("Committed revision " + committedRevision + "\n");
		sb.append(super.toString());
		return sb.toString();
	}

}
