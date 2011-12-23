/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.monitor.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.equinox.p2.core.UIServices.AuthenticationInfo;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.illinois.codingspectator.monitor.ui.Uploader;
import edu.illinois.codingspectator.monitor.ui.submission.Submitter;

/**
 * 
 * @author Mohsen Vakilian
 * 
 */
public class TestAuthentication {

	private static MockSubmitterFactory submitterFactory;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		submitterFactory= new MockSubmitterFactory();
	}

	@Test
	public void shouldRetryAuthentication() {
		AuthenticationInfo wrongAuthenticationInfo= new AuthenticationInfo("", submitterFactory.getPassword(), false);
		MockAuthenticationProvider mockAuthenticationProvider= new MockAuthenticationProvider(wrongAuthenticationInfo, submitterFactory.getAuthenticationInfo());
		Submitter submitter= new Submitter(mockAuthenticationProvider);
		boolean authenticated= Uploader.promptUntilValidCredentialsOrCanceled(submitter);
		assertEquals(MockSubmitterFactory.UUID, submitter.getUUID());
		assertEquals(2, mockAuthenticationProvider.getNumberOfTimesQueried());
		assertTrue(authenticated);
	}

	@Test
	public void shouldCancelAuthentication() {
		AuthenticationInfo wrongAuthenticationInfo= new AuthenticationInfo("", submitterFactory.getPassword(), false);
		MockAuthenticationProvider mockAuthenticationProvider= new MockAuthenticationProvider(wrongAuthenticationInfo, null);
		Submitter submitter= new Submitter(mockAuthenticationProvider);
		boolean authenticated= Uploader.promptUntilValidCredentialsOrCanceled(submitter);
		assertEquals(MockSubmitterFactory.UUID, submitter.getUUID());
		assertEquals(2, mockAuthenticationProvider.getNumberOfTimesQueried());
		assertFalse(authenticated);
	}


}
