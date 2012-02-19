/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.files.snapshoted;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import edu.illinois.codingtracker.helpers.Configuration;
import edu.illinois.codingtracker.helpers.ResourceHelper;
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

	public String getRevision() {
		return revision;
	}

	public String getCommittedRevision() {
		return committedRevision;
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
		if (!Configuration.isOldFormat) {
			revision= operationLexer.readString();
			committedRevision= operationLexer.readString();
		} else {
			revision= "";
			committedRevision= "";
		}
	}

	@Override
	public void replay() throws CoreException {
		IResource workspaceResource= ResourceHelper.findWorkspaceMember(resourcePath);
		if (workspaceResource != null && !isExternallyModifiedResource(resourcePath)) {
			//Match against the existing file.
			if (!fileContent.equals(ResourceHelper.readFileContent((IFile)workspaceResource))) {
				throw new RuntimeException("The snapshot file does not match the existing file: " + resourcePath);
			}
		} else {
			//If there is no existing file or it was externally modified, create the snapshoted compilation unit.
			super.replay();
		}
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
