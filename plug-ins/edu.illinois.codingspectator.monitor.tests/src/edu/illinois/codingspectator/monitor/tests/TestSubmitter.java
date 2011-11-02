/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.monitor.tests;

import static edu.illinois.codingspectator.monitor.tests.MockSubmitterFactory.FILENAME;
import static org.junit.Assert.assertEquals;
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
import edu.illinois.codingspectator.monitor.ui.submission.Submitter.AuthenticanResult;
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

	private static MockSubmitterFactory submitterFactory;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		submitterFactory= new MockSubmitterFactory();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		SVNCommitInfo deleteInfo= submitterFactory.getCommitClient().doDelete(new SVNURL[] { submitterFactory.getURLManager().getPersonalWorkspaceSVNURL() }, "Deleted test import");
		assertNotSame("The testing directory was not removed at the remote location.", SVNCommitInfo.NULL, deleteInfo);
	}

	private void assertWorkingCopyExists() throws SVNException {
		// Check that the working directory has been created locally.
		assertTrue("Failed to initialize the submitter.", new File(Submitter.WATCHED_FOLDER + File.separator + ".svn").exists());

		// Check that the directory has been created remotely.
		SVNInfo info= submitterFactory.getWorkingCopyClient().doInfo(submitterFactory.getURLManager().getPersonalWorkspaceSVNURL(), SVNRevision.HEAD, SVNRevision.HEAD);
		assertNotNull(info);
	}

	@Test
	public void shouldSubmit() throws SubmissionException, InitializationException, SVNException, CoreException {
		AuthenticanResult authenticanResult= submitterFactory.getSubmitter().authenticate();

		assertEquals(AuthenticanResult.OK, authenticanResult);

		submitterFactory.modifyFileInWatchedFolder();

		// Add and commit the local file that we created.
		submitterFactory.getSubmitter().submit();

		// Check that the file has been created remotely.
		SVNURL url= submitterFactory.getURLManager().getSVNURL(submitterFactory.getURLManager().joinByURLSeparator(submitterFactory.getURLManager().getPersonalWorkspaceURL(), FILENAME));
		SVNInfo info= submitterFactory.getWorkingCopyClient().doInfo(url, SVNRevision.HEAD, SVNRevision.HEAD);
		assertNotNull(info);

		assertWorkingCopyExists();
	}

}
