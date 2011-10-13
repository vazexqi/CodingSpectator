/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.monitor.tests;

import static edu.illinois.codingspectator.monitor.tests.SubmitterHelper.initializeSubmitter;
import static edu.illinois.codingspectator.monitor.tests.SubmitterHelper.modifyFileInWatchedFolder;
import static edu.illinois.codingspectator.monitor.tests.SubmitterHelper.submitter;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tmatesoft.svn.core.SVNException;

import edu.illinois.codingspectator.efs.EFSFile;
import edu.illinois.codingspectator.monitor.ui.submission.Submitter;
import edu.illinois.codingspectator.monitor.ui.submission.Submitter.AuthenticanResult;
import edu.illinois.codingspectator.monitor.ui.submission.Submitter.InitializationException;
import edu.illinois.codingspectator.monitor.ui.submission.Submitter.SubmissionException;

public class TestSubmitterWithConflicts {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		initializeSubmitter();
	}

	@Test
	public void shouldSubmitOutdatedWorkingCopy() throws SubmissionException, InitializationException, SVNException, CoreException {
		modifyLog();
		submit();

		long initialRevisionNumber= SubmitterHelper.getFileRevisionNumber();

		modifyLog();
		submit();

		assertTrue(SubmitterHelper.getFileRevisionNumber() > initialRevisionNumber);
	}

	@Test
	public void shouldSubmitConflictedWorkingCopy() throws SubmissionException, InitializationException, SVNException, CoreException {
		modifyLog();
		submit();

		long initialRevisionNumber= SubmitterHelper.getFileRevisionNumber();

		modifyLog();
		SubmitterHelper.svnManager.doUpdate();
		submit();

		assertTrue(SubmitterHelper.getFileRevisionNumber() > initialRevisionNumber);
	}

	private void modifyLog() throws CoreException {
		cleanWatchedFolder();
		makeWatchedFolderOutdated();
		modifyFileInWatchedFolder();
	}

	private void submit() throws InitializationException, SubmissionException {
		AuthenticanResult authenticanResult= submitter.authenticate();
		assertEquals(AuthenticanResult.OK, authenticanResult);
		submitter.submit();
	}

	private void makeWatchedFolderOutdated() throws CoreException {
		EFSFile outdatedWatchedFolder= new EFSFile("outdated-watched-folder");
		EFSFile watchedFolder= new EFSFile(Submitter.WATCHED_FOLDER);
		outdatedWatchedFolder.copyTo(watchedFolder);
	}

	private void cleanWatchedFolder() throws CoreException {
		new EFSFile(Submitter.WATCHED_FOLDER).delete();
	}

}
