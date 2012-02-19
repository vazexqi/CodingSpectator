/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.monitor.core.authentication;

import java.io.IOException;

import org.eclipse.equinox.p2.core.UIServices.AuthenticationInfo;
import org.eclipse.equinox.security.storage.StorageException;

/**
 * Interface to provide username and password for any forms of authentication.
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public interface AuthenticationProvider {

	public abstract void clearSecureStorage() throws IOException;

	public abstract AuthenticationInfo findUsernamePassword();

	public abstract void saveAuthenticationInfo(AuthenticationInfo authenticationInfo) throws StorageException, IOException;

	public abstract String getRepositoryURL();

}
