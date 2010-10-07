package edu.illinois.codingspectator.monitor.tests;

import java.io.IOException;

import org.eclipse.equinox.p2.core.UIServices.AuthenticationInfo;

import edu.illinois.codingspectator.monitor.authentication.AuthenticationProvider;

/**
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class MockAuthenticationProvider implements AuthenticationProvider {

	@Override
	public AuthenticationInfo findUsernamePassword() {
		return new AuthenticationInfo("nchen", "nchen", false);
	}

	@Override
	public void clearSecureStorage() throws IOException {
	}

	@Override
	public void saveAuthenticationInfo(AuthenticationInfo authenticationInfo) {
	}

}
