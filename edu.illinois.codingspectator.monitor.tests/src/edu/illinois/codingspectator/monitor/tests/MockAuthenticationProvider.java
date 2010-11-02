/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.monitor.tests;

import java.io.IOException;

import org.eclipse.equinox.p2.core.UIServices.AuthenticationInfo;

import edu.illinois.codingspectator.monitor.core.authentication.AuthenticationProvider;

/**
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class MockAuthenticationProvider implements AuthenticationProvider {

	@Override
	public AuthenticationInfo findUsernamePassword() {
		return new AuthenticationInfo("nchen", "nchen", false); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public void clearSecureStorage() throws IOException {
	}

	@Override
	public void saveAuthenticationInfo(AuthenticationInfo authenticationInfo) {
	}

	@Override
	public String getRepositoryURL() {
		return Messages.MockAuthenticationProvider_TestRepositoryURL;
	}

}
