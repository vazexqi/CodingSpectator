/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.monitor.ui.prefs;

import java.io.IOException;

import org.eclipse.equinox.p2.core.UIServices;
import org.eclipse.equinox.p2.core.UIServices.AuthenticationInfo;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;

import edu.illinois.codingspectator.monitor.ui.Messages;

/**
 * 
 * @author nchen
 * @author Mohsen Vakilian
 * 
 */
public class SecureStorageFacade {

	private ISecurePreferences getSecurePreferencesNode() {
		ISecurePreferences securePreferences= SecurePreferencesFactory.getDefault();
		String nodeName= Messages.SecureStorage_AuthenticationNodeName;
		if (securePreferences.nodeExists(nodeName)) {
			return securePreferences.node(nodeName);
		} else
			return null;
	}

	private boolean securePreferencesNodeExists() {
		return getSecurePreferencesNode() != null;
	}

	private ISecurePreferences getSecurePreferencesNodeLazily() {
		ISecurePreferences securePreferences= SecurePreferencesFactory.getDefault();
		String nodeName= Messages.SecureStorage_AuthenticationNodeName;
		return securePreferences.node(nodeName);
	}

	public void clearSecureStorage() throws IOException {
		if (securePreferencesNodeExists()) {
			ISecurePreferences prefNode= getSecurePreferencesNode();
			prefNode.removeNode();
			prefNode.flush();
		}
	}

	/**
	 * @see org.eclipse.equinox.internal.p2.repository.Credentials.forLocation(URI, boolean,
	 *      AuthenticationInfo)
	 */
	public AuthenticationInfo getStoredAuthenticationInfo() throws IOException {
		if (securePreferencesNodeExists()) {
			ISecurePreferences prefNode= getSecurePreferencesNode();
			String username= null, password= null;
			try {
				username= prefNode.get(Messages.SecureStorage_UsernameKey, null);
				password= prefNode.get(Messages.SecureStorage_PasswordKey, null);
				if (username != null && password != null)
					return new UIServices.AuthenticationInfo(username, password, true);
				else {
					clearSecureStorage();
					return null;
				}
			} catch (StorageException e) {
				if (e.getErrorCode() != StorageException.NO_PASSWORD)
					clearSecureStorage();
				return null;
			}
		} else {
			return null;
		}
	}

	public void saveAuthenticationInfo(UIServices.AuthenticationInfo authenticationInfo) throws IOException {
		if (authenticationInfo.saveResult()) {
			try {
				ISecurePreferences prefNode= getSecurePreferencesNodeLazily();
				prefNode.put(Messages.SecureStorage_UsernameKey, authenticationInfo.getUserName(), true);
				prefNode.put(Messages.SecureStorage_PasswordKey, authenticationInfo.getPassword(), true);
				prefNode.flush();
			} catch (Exception e) {
				clearSecureStorage();
			}
		} else {
			// if persisted earlier - the preference should be removed
			clearSecureStorage();
		}
	}

}
