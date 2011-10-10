/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.monitor.tests;

import static edu.illinois.codingspectator.monitor.tests.SubmitterHelper.FILENAME;
import static edu.illinois.codingspectator.monitor.tests.SubmitterHelper.commitClient;
import static edu.illinois.codingspectator.monitor.tests.SubmitterHelper.initializeSubmitter;
import static edu.illinois.codingspectator.monitor.tests.SubmitterHelper.modifyFileInWatchedFolder;
import static edu.illinois.codingspectator.monitor.tests.SubmitterHelper.submitter;
import static edu.illinois.codingspectator.monitor.tests.SubmitterHelper.urlManager;
import static edu.illinois.codingspectator.monitor.tests.SubmitterHelper.workingCopyClient;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNRevision;

import edu.illinois.codingspectator.monitor.ui.submission.Submitter;
import edu.illinois.codingspectator.monitor.ui.submission.Submitter.CanceledDialogException;
import edu.illinois.codingspectator.monitor.ui.submission.Submitter.FailedAuthenticationException;
import edu.illinois.codingspectator.monitor.ui.submission.Submitter.InitializationException;
import edu.illinois.codingspectator.monitor.ui.submission.Submitter.SubmissionException;

/**
 * This class tests the submission process: we can import a directory and check it out; we can add
 * and commit.
 * 
 * At the end of the tests, it cleans up the repository; leaving no trace in the current
 * Revision.HEAD that this test has occurred. To check that this test has actually been run, check
 * the log of the repository e.g. 'svn log'.
 * 
 * There are some duplication in the tests. This is intentional so that the tests are easier to
 * read.
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class TestSubmitter {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		initializeSubmitter();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		SVNCommitInfo deleteInfo= commitClient.doDelete(new SVNURL[] { urlManager.getPersonalWorkspaceSVNURL() }, "Deleted test import");
		assertNotSame("The testing directory was not removed at the remote location.", SVNCommitInfo.NULL, deleteInfo);
	}

	private void shouldInitialize() throws InitializationException, SVNException, FailedAuthenticationException, CanceledDialogException {
		submitter.authenticateAndInitialize(); // This call is idempotent and can be called multiple times without affecting the state of the system.

		// Check that the working directory has been created locally.
		assertTrue("Failed to initialize the submitter.", new File(Submitter.WATCHED_FOLDER + File.separator + ".svn").exists());

		// Check that the directory has been created remotely.
		SVNInfo info= workingCopyClient.doInfo(urlManager.getPersonalWorkspaceSVNURL(), SVNRevision.HEAD, SVNRevision.HEAD);
		assertNotNull(info);
	}

	@Test
	public void shouldSubmit() throws SubmissionException, InitializationException, SVNException, CoreException, FailedAuthenticationException, CanceledDialogException {
		submitter.authenticateAndInitialize(); // This call is idempotent and can be called multiple times without affecting the state of the system.

		modifyFileInWatchedFolder();

		// Add and commit the local file that we created.
		submitter.submit();

		// Check that the file has been created remotely.
		SVNURL url= urlManager.getSVNURL(urlManager.joinByURLSeparator(urlManager.getPersonalRepositoryURL(), FILENAME));
		SVNInfo info= workingCopyClient.doInfo(url, SVNRevision.HEAD, SVNRevision.HEAD);
		assertNotNull(info);

		shouldInitialize();
	}

}
