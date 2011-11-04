/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.monitor.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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

/**
 * 
 * @author Mohsen Vakilian
 * 
 */
public class TestSubmitterWithConflicts {

	private static MockSubmitterFactory submitterFactory;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		submitterFactory= new MockSubmitterFactory();
	}

	@Test
	public void shouldSubmitOutdatedWorkingCopy() throws SubmissionException, InitializationException, SVNException, CoreException {
		modifyLog();
		submit();

		long initialRevisionNumber= submitterFactory.getFileRevisionNumber();

		modifyLog();
		assertFalse(submitterFactory.getSubmitter().doLocalAndRemoteDataMatch());
		submit();

		assertEquals(initialRevisionNumber + 2, submitterFactory.getFileRevisionNumber());
	}

	@Test
	public void shouldSubmitConflictedWorkingCopy() throws SubmissionException, InitializationException, SVNException, CoreException {
		modifyLog();
		submit();

		long initialRevisionNumber= submitterFactory.getFileRevisionNumber();

		modifyLog();
		submitterFactory.getSVNManager().doUpdate();
		assertTrue(submitterFactory.getSubmitter().doLocalAndRemoteDataMatch());
		submit();

		assertEquals(initialRevisionNumber + 2, submitterFactory.getFileRevisionNumber());
	}

	private void modifyLog() throws CoreException {
		cleanWatchedFolder();
		makeWatchedFolderOutdated();
		submitterFactory.modifyFileInWatchedFolder();
	}

	private void submit() throws InitializationException, SubmissionException {
		AuthenticanResult authenticanResult= submitterFactory.getSubmitter().authenticate();
		assertEquals(MockSubmitterFactory.UUID, submitterFactory.getSubmitter().getUUID());
		assertEquals(AuthenticanResult.OK, authenticanResult);
		submitterFactory.getSubmitter().submit();
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
