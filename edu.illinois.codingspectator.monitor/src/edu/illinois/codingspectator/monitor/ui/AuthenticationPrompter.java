/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.monitor.ui;

import java.io.IOException;
import java.text.MessageFormat;

import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.p2.core.UIServices;
import org.eclipse.equinox.p2.core.UIServices.AuthenticationInfo;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import edu.illinois.codingspectator.monitor.Activator;
import edu.illinois.codingspectator.monitor.Messages;
import edu.illinois.codingspectator.monitor.authentication.AuthenticationProvider;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class AuthenticationPrompter implements AuthenticationProvider {

	DialogState dialogState= new DialogState();

	/**
	 * @see org.eclipse.equinox.internal.p2.ui.ValidationDialogServiceUI.getUsernamePassword(String)
	 * 
	 */
	private AuthenticationInfo getUsernamePassword(final String loginDestination) {

		// Only a final reference can be assigned to inside an anonymous class. This is why they put a single object inside an array.
		final AuthenticationInfo[] result= new AuthenticationInfo[1];
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				Shell shell= getDefaultParentShell();
				String message= MessageFormat.format(dialogState.getDialogDescription(), loginDestination);
				String dialogTitle= MessageFormat.format(Messages.AuthenticationPrompter_DialogTitle, loginDestination);
				String username= getSVNWorkingCopyUsername();
				UserValidationDialog dialog= new UserValidationDialog(shell, dialogTitle, message, username, dialogState.getDialogType());
				if (dialog.open() == Window.OK) {
					result[0]= dialog.getResult();
				} else { // If cancel was invoked
					result[0]= null;
				}
			}

		});
		dialogState.changeState();
		return result[0];
	}

	protected String getSVNWorkingCopyUsername() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Returns a shell that is appropriate to use as the parent for a modal dialog.
	 * 
	 * @see org.eclipse.equinox.internal.p2.ui.ProvUI.getDefaultParentShell()
	 */
	private static Shell getDefaultParentShell() {
		return PlatformUI.getWorkbench().getModalDialogShellProvider().getShell();
	}

	@Override
	public void clearSecureStorage() throws IOException {
		if (securePreferencesNodeExists()) {
			ISecurePreferences prefNode= getSecurePreferencesNode();
			prefNode.removeNode();
			prefNode.flush();
		}
	}

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

	/**
	 * @see org.eclipse.equinox.internal.p2.repository.Credentials.forLocation(URI, boolean,
	 *      AuthenticationInfo)
	 */
	private AuthenticationInfo askOrLookupCredentials() throws IOException {
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
				clearSecureStorage();
				return null;
			}
		}

		return getUsernamePassword(Messages.WorkbenchPreferencePage_PluginName);
	}

	@Override
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

	/* (non-Javadoc)
	 * @see edu.illinois.refactoringwatcher.monitor.authentication.AuthenticationProvider#findUsernamePassword()
	 */
	@Override
	public AuthenticationInfo findUsernamePassword() {
		try {
			return askOrLookupCredentials();
		} catch (Exception ex) {
			Status errorStatus= Activator.getDefault().createErrorStatus(Messages.AuthenticationPrompter_FailureMessage, ex);
			Activator.getDefault().log(errorStatus);
		}
		return null;
	}

}
