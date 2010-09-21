package edu.illinois.refactoringwatcher.monitor.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNCommitClient;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCClient;

import edu.illinois.refactoringwatcher.monitor.Messages;
import edu.illinois.refactoringwatcher.monitor.prefs.PrefsFacade;
import edu.illinois.refactoringwatcher.monitor.submission.Submitter;
import edu.illinois.refactoringwatcher.monitor.submission.Submitter.InitializationException;
import edu.illinois.refactoringwatcher.monitor.submission.Submitter.SubmissionException;

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


	private static final String USERNAME= "nchen";

	private static final String PASSWORD= "nchen";

	private static final String UUID_FOR_TESTING= "00000000-0000-0000-0000-000000000000";

	private static final String FILENAME= "foo";

	private static Submitter submitter;

	private static SVNWCClient workingCopyClient;

	private static SVNCommitClient commitClient;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// Set up the submitter to the default repository location
		PrefsFacade.setNetid(USERNAME);
		PrefsFacade.setUUID(UUID_FOR_TESTING);
		submitter= new Submitter(USERNAME, PASSWORD);

		//Create a new SVNWCClient directly to be used to verify repository properties
		SVNClientManager clientManager= SVNClientManager.newInstance(null, USERNAME, PASSWORD);
		workingCopyClient= clientManager.getWCClient();
		commitClient= clientManager.getCommitClient();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		// Delete the repository location that we tested
		SVNURL url= SVNURL.parseURIEncoded(Messages.Submitter_repository_base_url + "/" + USERNAME + "/" + UUID_FOR_TESTING);
		SVNCommitInfo deleteInfo= commitClient.doDelete(new SVNURL[] { url }, "Deleted test import");
		assertNotSame("The testing directory was not removed at the remote location.", SVNCommitInfo.NULL, deleteInfo);
	}

	@Test
	public void shouldInitialize() throws InitializationException, SVNException {
		submitter.initialize(); // This call is idempotent and can be called multiple times without affecting the state of the system.

		// Check that the working directory has been created locally.
		assertTrue("Failed to initialize the submitter.", new File(Submitter.watchedDirectory + File.separator + ".svn").exists());

		// Check that the directory has been created remotely.
		SVNURL url= SVNURL.parseURIEncoded(Messages.Submitter_repository_base_url + "/" + USERNAME + "/" + UUID_FOR_TESTING);
		SVNInfo info= workingCopyClient.doInfo(url, SVNRevision.HEAD, SVNRevision.HEAD);
		assertNotNull(info);
	}

	@Test
	public void shouldSubmit() throws SubmissionException, InitializationException, SVNException, CoreException {
		submitter.initialize(); // This call is idempotent and can be called multiple times without affecting the state of the system.

		createTempFileLocally();

		// Add and commit the local file that we created.
		submitter.submit();

		// Check that the file has been created remotely.
		SVNURL url= SVNURL.parseURIEncoded(Messages.Submitter_repository_base_url + "/" + USERNAME + "/" + UUID_FOR_TESTING + "/" + FILENAME);
		SVNInfo info= workingCopyClient.doInfo(url, SVNRevision.HEAD, SVNRevision.HEAD);
		assertNotNull(info);
	}

	private void createTempFileLocally() throws CoreException {
		// Create a file that will be added and committed.
		IPath LTKdataLocation= Platform.getStateLocation(Platform.getBundle(Messages.Submitter_ltk_bundle_name));
		IFileStore fileStore= EFS.getLocalFileSystem().getStore(LTKdataLocation.append(FILENAME));
		OutputStream outputStream= fileStore.openOutputStream(EFS.ATTRIBUTE_GROUP_READ | EFS.ATTRIBUTE_GROUP_WRITE, new NullProgressMonitor());
		PrintWriter printWriter= new PrintWriter(outputStream);
		printWriter.write("Testing");
		printWriter.flush();
	}

}
