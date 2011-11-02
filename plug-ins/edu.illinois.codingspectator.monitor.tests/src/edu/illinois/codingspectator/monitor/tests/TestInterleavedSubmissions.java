/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.monitor.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
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
public class TestInterleavedSubmissions {

	@Test
	public void submissionsShouldBeEfficientWhenInterleaved() throws CoreException, InitializationException, SubmissionException, SVNException {
		MockSubmitterFactory submitterFactory1= new MockSubmitterFactory(MockParticipantFactory.getMockParticipant(0));
		MockSubmitterFactory submitterFactory2= new MockSubmitterFactory(MockParticipantFactory.getMockParticipant(1));

		submitterFactory1.modifyFileInWatchedFolder();
		submit(submitterFactory1);
		long initialRevisionNumber= submitterFactory1.getFileRevisionNumber();
		EFSFile watchedFolder= new EFSFile(Submitter.WATCHED_FOLDER);
		EFSFile participant1BackupOfWatchedFolder= new EFSFile(Submitter.WATCHED_FOLDER + "_participant1_bak");
		watchedFolder.moveTo(participant1BackupOfWatchedFolder);
		submitterFactory2.modifyFileInWatchedFolder();
		submit(submitterFactory2);
		watchedFolder.delete();
		participant1BackupOfWatchedFolder.moveTo(watchedFolder);
		submitterFactory1.modifyFileInWatchedFolder();
		assertTrue("Detected an inconsistency between local and remote data when one didn't exist.", submitterFactory1.getSubmitter().doLocalAndRemoteDataMatch());
		submit(submitterFactory1);
		assertEquals(initialRevisionNumber + 2, submitterFactory1.getFileRevisionNumber());
	}

	private void submit(MockSubmitterFactory submitterFactory) throws InitializationException, SubmissionException {
		AuthenticanResult authenticanResult= submitterFactory.getSubmitter().authenticate();
		assertEquals(MockSubmitterFactory.UUID, submitterFactory.getSubmitter().getUUID());
		assertEquals(AuthenticanResult.OK, authenticanResult);
		submitterFactory.getSubmitter().submit();
	}

}
