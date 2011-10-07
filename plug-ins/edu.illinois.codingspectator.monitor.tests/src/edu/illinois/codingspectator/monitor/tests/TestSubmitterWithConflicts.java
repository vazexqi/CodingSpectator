/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.monitor.tests;

import static edu.illinois.codingspectator.monitor.tests.SubmitterHelper.initializeSubmitter;
import static edu.illinois.codingspectator.monitor.tests.SubmitterHelper.modifyFileInWatchedFolder;
import static edu.illinois.codingspectator.monitor.tests.SubmitterHelper.submitter;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tmatesoft.svn.core.SVNException;

import edu.illinois.codingspectator.efs.EFSFile;
import edu.illinois.codingspectator.monitor.ui.submission.Submitter;
import edu.illinois.codingspectator.monitor.ui.submission.Submitter.CanceledDialogException;
import edu.illinois.codingspectator.monitor.ui.submission.Submitter.FailedAuthenticationException;
import edu.illinois.codingspectator.monitor.ui.submission.Submitter.InitializationException;
import edu.illinois.codingspectator.monitor.ui.submission.Submitter.SubmissionException;

public class TestSubmitterWithConflicts {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		initializeSubmitter();
	}

	@Test
	public void shouldSubmit() throws SubmissionException, InitializationException, SVNException, CoreException, FailedAuthenticationException, CanceledDialogException {
		modifyLogAndSubmit();

		long initialRevisionNumber= SubmitterHelper.getFileRevisionNumber();

		modifyLogAndSubmit();

		assertTrue(SubmitterHelper.getFileRevisionNumber() > initialRevisionNumber);
	}

	private void modifyLogAndSubmit() throws CoreException, InitializationException, FailedAuthenticationException, CanceledDialogException, SubmissionException {
		cleanWatchedFolder();
		makeWatchedFolderOutdated();
		modifyFileInWatchedFolder();
		submitter.authenticateAndInitialize();
		submitter.submit();
	}

	private void makeWatchedFolderOutdated() throws CoreException {
		EFSFile outdatedWatchedFolder= new EFSFile("outdated-watched-folder");
		EFSFile watchedFolder= new EFSFile(Submitter.WATCHED_DIRECTORY);
		outdatedWatchedFolder.copyTo(watchedFolder);
	}

	private void cleanWatchedFolder() {
		new File(Submitter.WATCHED_DIRECTORY).delete();
	}

}
