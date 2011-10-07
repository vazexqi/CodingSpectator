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

	private final String username;

	private final String password;

	public MockAuthenticationProvider(String username, String password) {
		this.username= username;
		this.password= password;
	}

	@Override
	public AuthenticationInfo findUsernamePassword() {
		return new AuthenticationInfo(username, password, false); //$NON-NLS-1$ //$NON-NLS-2$
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
