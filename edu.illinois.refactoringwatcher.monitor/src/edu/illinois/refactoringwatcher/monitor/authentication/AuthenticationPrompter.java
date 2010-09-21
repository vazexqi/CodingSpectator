package edu.illinois.refactoringwatcher.monitor.authentication;

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

import edu.illinois.refactoringwatcher.monitor.Activator;
import edu.illinois.refactoringwatcher.monitor.Messages;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class AuthenticationPrompter {

	/**
	 * @see org.eclipse.equinox.internal.p2.ui.ValidationDialogServiceUI.getUsernamePassword(String)
	 * 
	 */
	public static AuthenticationInfo getUsernamePassword(final String location) {

		// Only a final reference can be assigned to inside an anonymous class. This is why they put a single object inside an array.
		final AuthenticationInfo[] result= new AuthenticationInfo[1];
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				Shell shell= getDefaultParentShell();
				String message= MessageFormat.format(Messages.AuthenticationPrompter_DialogDescription, location);
				UserValidationDialog dialog= new UserValidationDialog(shell, Messages.AuthenticationPrompter_DialogTitle, null, message);
				if (dialog.open() == Window.OK) {
					result[0]= dialog.getResult();
				}
			}

		});
		return result[0];
	}

	/**
	 * Returns a shell that is appropriate to use as the parent for a modal dialog.
	 * 
	 * @see org.eclipse.equinox.internal.p2.ui.ProvUI.getDefaultParentShell()
	 */
	public static Shell getDefaultParentShell() {
		return PlatformUI.getWorkbench().getModalDialogShellProvider().getShell();
	}

	/**
	 * @throws StorageException
	 * @throws IOException
	 * @see org.eclipse.equinox.internal.p2.repository.Credentials.forLocation(URI, boolean,
	 *      AuthenticationInfo)
	 */
	public static AuthenticationInfo getCredentialsForLocation() throws StorageException, IOException {
		UIServices.AuthenticationInfo loginDetails= null;
		ISecurePreferences securePreferences= null;
		securePreferences= SecurePreferencesFactory.getDefault();
		String nodeName= Messages.AuthenticationPrompter_SecureStorageNodeName;
		String username= null, password= null;
		ISecurePreferences prefNode= null;

		if (securePreferences.nodeExists(nodeName)) {
			prefNode= securePreferences.node(nodeName);
			if (prefNode != null) {
				username= prefNode.get(Messages.AuthenticationPrompter_username, null);
				password= prefNode.get(Messages.AuthenticationPrompter_password, null);
			}
			if (username != null && password != null)
				return new UIServices.AuthenticationInfo(username, password, true);
		}

		loginDetails= getUsernamePassword(Messages.WorkbenchPreferencePage_PluginName);

		if (loginDetails == null)
			return null;

		if (loginDetails.saveResult()) {
			if (prefNode == null)
				prefNode= securePreferences.node(nodeName);
			prefNode.put(Messages.AuthenticationPrompter_username, loginDetails.getUserName(), true);
			prefNode.put(Messages.AuthenticationPrompter_password, loginDetails.getPassword(), true);
			prefNode.flush();
		} else {
			// if persisted earlier - the preference should be removed
			if (securePreferences.nodeExists(nodeName)) {
				prefNode= securePreferences.node(nodeName);
				prefNode.removeNode();
				prefNode.flush();
			}
		}

		return loginDetails;
	}

	public static AuthenticationInfo findUsernamePassword() {
		try {
			AuthenticationInfo authenticationInfo= AuthenticationPrompter.getCredentialsForLocation();
			return authenticationInfo;
		} catch (Exception ex) {
			Status errorStatus= Activator.getDefault().createErrorStatus(Messages.AuthenticationPrompter_FailureMessage, ex);
			Activator.getDefault().log(errorStatus);
		}
		return null;
	}

}
