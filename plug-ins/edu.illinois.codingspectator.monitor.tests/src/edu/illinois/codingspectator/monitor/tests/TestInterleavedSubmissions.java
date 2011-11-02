/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.monitor.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.junit.Test;
import org.tmatesoft.svn.core.SVNException;

import edu.illinois.codingspectator.monitor.ui.submission.Submitter.AuthenticanResult;
import edu.illinois.codingspectator.monitor.ui.submission.Submitter.InitializationException;
import edu.illinois.codingspectator.monitor.ui.submission.Submitter.SubmissionException;

public class TestInterleavedSubmissions {

	@Test
	public void submissionsShouldBeEfficientWhenInterleaved() throws CoreException, InitializationException, SubmissionException, SVNException {
		MockSubmitterFactory submitterFactory1= new MockSubmitterFactory(MockParticipantFactory.getMockParticipant(0));
		MockSubmitterFactory submitterFactory2= new MockSubmitterFactory(MockParticipantFactory.getMockParticipant(1));

		submitterFactory1.modifyFileInWatchedFolder();
		submit(submitterFactory1);
		submitterFactory2.modifyFileInWatchedFolder();
		submit(submitterFactory2);
		submitterFactory1.modifyFileInWatchedFolder();
		// CodingSpectator should not detect an inconsistency when performing the following submission operation.
		assertTrue(submitterFactory1.getSubmitter().doLocalAndRemoteDataMatch());
		submit(submitterFactory1);
	}

	private void submit(MockSubmitterFactory submitterFactory) throws InitializationException, SubmissionException {
		AuthenticanResult authenticanResult= submitterFactory.getSubmitter().authenticate();
		assertEquals(AuthenticanResult.OK, authenticanResult);
		submitterFactory.getSubmitter().submit();
	}

}
