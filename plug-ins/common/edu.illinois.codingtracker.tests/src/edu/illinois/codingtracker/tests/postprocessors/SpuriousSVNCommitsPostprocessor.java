/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors;

import java.util.LinkedList;
import java.util.List;

import edu.illinois.codingtracker.helpers.ResourceHelper;
import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.files.UpdatedFileOperation;
import edu.illinois.codingtracker.operations.files.snapshoted.SVNCommittedFileOperation;
import edu.illinois.codingtracker.operations.resources.ExternallyModifiedResourceOperation;
import edu.illinois.codingtracker.operations.resources.ResourceOperation;
import edu.illinois.codingtracker.recording.TextRecorder;


/**
 * This class replaces spurious SVN commit operations followed by external change operations with
 * the corresponding update operations.
 * 
 * @author Stas Negara
 * 
 */
public class SpuriousSVNCommitsPostprocessor extends CodingTrackerPostprocessor {

	private final List<SVNCommittedFileOperation> svnCommits= new LinkedList<SVNCommittedFileOperation>();

	private final List<ExternallyModifiedResourceOperation> externalModifications= new LinkedList<ExternallyModifiedResourceOperation>();

	private int fixedSpuriousCommitsCount;


	@Override
	protected void checkPostprocessingPreconditions() {
		//no preconditions
	}

	@Override
	protected boolean shouldPostprocessVersionFolder(String folderName) {
		return true;
	}

	@Override
	protected void postprocess(List<UserOperation> userOperations) {
		fixedSpuriousCommitsCount= 0;
		boolean isCollectingCommits= false;
		boolean isCollectingExternalChanges= false;
		for (UserOperation userOperation : userOperations) {
			if (userOperation instanceof SVNCommittedFileOperation) {
				if (isCollectingExternalChanges) {
					postprocessCollectedOperations(); //First, postprocess the previously collected operations.
					isCollectingExternalChanges= false;
				}
				svnCommits.add((SVNCommittedFileOperation)userOperation);
				isCollectingCommits= true;
			} else if (isCollectingCommits && userOperation instanceof ExternallyModifiedResourceOperation) {
				externalModifications.add((ExternallyModifiedResourceOperation)userOperation);
				isCollectingExternalChanges= true;
			} else {
				if (isCollectingCommits) {
					postprocessCollectedOperations();
					isCollectingCommits= false;
					isCollectingExternalChanges= false;
				}
				TextRecorder.record(userOperation);
			}
		}
		//In case there are no trailing user operations after SVN commit or external change operations.
		if (isCollectingCommits) {
			postprocessCollectedOperations();
		}
		System.out.println("Fixed spurious commits count: " + fixedSpuriousCommitsCount);
	}

	private void postprocessCollectedOperations() {
		List<ResourceOperation> resultingOperations= new LinkedList<ResourceOperation>();
		for (ExternallyModifiedResourceOperation externalModification : externalModifications) {
			UpdatedFileOperation updatedFileOperation= postprocessCollectedExternalModification(externalModification);
			if (updatedFileOperation != null) {
				resultingOperations.add(updatedFileOperation);
				fixedSpuriousCommitsCount++;
				System.out.println("Timestamp: " + updatedFileOperation.getTime());
			} else {
				resultingOperations.add(externalModification);
			}
		}
		for (SVNCommittedFileOperation svnCommit : svnCommits) { //Record remaining, not spurious SVN commits.
			TextRecorder.record(svnCommit);
		}
		for (ResourceOperation resourceOperation : resultingOperations) {
			TextRecorder.record(resourceOperation);
		}
		svnCommits.clear();
		externalModifications.clear();
	}

	/**
	 * Returns an UpdatedFileOperation to replace the given spurious external modification or null
	 * if the given external modification is not spurious.
	 * 
	 * @param externalModification
	 * @return
	 */
	private UpdatedFileOperation postprocessCollectedExternalModification(ExternallyModifiedResourceOperation externalModification) {
		for (int i= 0; i < svnCommits.size(); i++) {
			SVNCommittedFileOperation svnCommit= svnCommits.get(i);
			if (isSpuriousPair(svnCommit, externalModification)) {
				svnCommits.remove(i);
				return new UpdatedFileOperation(externalModification.getResourcePath(), svnCommit.getRevision(),
												svnCommit.getCommittedRevision(), externalModification.getTime());
			}
		}
		return null;
	}

	private boolean isSpuriousPair(SVNCommittedFileOperation svnCommit, ExternallyModifiedResourceOperation externalModification) {
		return areCloseEnough(svnCommit, externalModification) &&
				svnCommit.getResourcePath().equals(externalModification.getResourcePath());
	}

	private boolean areCloseEnough(SVNCommittedFileOperation svnCommit, ExternallyModifiedResourceOperation externalModification) {
		final long maxDelta= 6 * 1000; // 6 seconds
		long svnCommitTime= svnCommit.getTime();
		long externalModificationTime= externalModification.getTime();
		return svnCommitTime < externalModificationTime && externalModificationTime - svnCommitTime <= maxDelta;
	}

	@Override
	protected String getResultFilePostfix() {
		return ".fixed_spurious_commits";
	}

	@Override
	protected String getResult() {
		return ResourceHelper.readFileContent(mainRecordFile);
	}

}
