package edu.illinois.refactoringwatcher.monitor.authentication;

import org.eclipse.equinox.p2.core.UIServices.AuthenticationInfo;

/**
 * Interface to provide username and password for any forms of authentication.
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public interface AuthenticationProvider {

	public abstract AuthenticationInfo findUsernamePassword();

}
