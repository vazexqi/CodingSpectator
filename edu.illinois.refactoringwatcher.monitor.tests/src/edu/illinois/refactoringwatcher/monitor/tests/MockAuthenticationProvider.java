package edu.illinois.refactoringwatcher.monitor.tests;

import org.eclipse.equinox.p2.core.UIServices.AuthenticationInfo;

import edu.illinois.refactoringwatcher.monitor.authentication.AuthenticationProvider;

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

}
