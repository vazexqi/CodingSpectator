/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.monitor.tests;

import java.io.IOException;

import org.eclipse.equinox.p2.core.UIServices.AuthenticationInfo;

import edu.illinois.codingspectator.monitor.core.authentication.AuthenticationProvider;

/**
 * 
 * This authentication provider provides a wrong authentication information the first time it is
 * asked for and provides the correct authentication information the second time.
 * 
 * @author Mohsen Vakilian
 * 
 */
public class MockAuthenticationProvider implements AuthenticationProvider {

	private int numberOfTimesQueried= 0;

	private final AuthenticationInfo[] authenticationInfos;

	public MockAuthenticationProvider(AuthenticationInfo... authenticationInfos) {
		this.authenticationInfos= authenticationInfos;
	}

	@Override
	public AuthenticationInfo findUsernamePassword() {
		AuthenticationInfo nextAuthenticationInfo= authenticationInfos[numberOfTimesQueried % authenticationInfos.length];
		++numberOfTimesQueried;
		return nextAuthenticationInfo;
	}

	public int getNumberOfTimesQueried() {
		return numberOfTimesQueried;
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
