/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.monitor.ui;

import java.io.IOException;
import java.text.MessageFormat;

import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.p2.core.UIServices;
import org.eclipse.equinox.p2.core.UIServices.AuthenticationInfo;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import edu.illinois.codingspectator.monitor.Activator;
import edu.illinois.codingspectator.monitor.Messages;
import edu.illinois.codingspectator.monitor.authentication.AuthenticationProvider;
import edu.illinois.codingspectator.monitor.prefs.SecureStorageFacade;
import edu.illinois.codingspectator.monitor.submission.SVNManager;
import edu.illinois.codingspectator.monitor.ui.submission.Submitter;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class AuthenticationPrompter implements AuthenticationProvider {

	DialogState dialogState= new DialogState();

	SecureStorageFacade secureStorageFacade= new SecureStorageFacade();

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
				UserValidationDialog dialog= setupDialog(loginDestination);
				if (dialog.open() == Window.OK) {
					result[0]= dialog.getResult();
				} else { // If cancel was invoked
					result[0]= null;
				}
			}

			private UserValidationDialog setupDialog(final String loginDestination) {
				Shell shell= getDefaultParentShell();
				String dialogTitle= MessageFormat.format(Messages.AuthenticationPrompter_DialogTitle, loginDestination);
				String username= new SVNManager(Submitter.watchedDirectory).getSVNWorkingCopyUsername();
				String message= MessageFormat.format(dialogState.getDialogDescription(), loginDestination);

				UserValidationDialog dialog= new UserValidationDialog(shell, dialogTitle, message, username, dialogState.getDialogType());
				return dialog;
			}

		});
		dialogState.changeState();
		return result[0];
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
		secureStorageFacade.clearSecureStorage();
	}


	private AuthenticationInfo askOrLookupCredentials() throws IOException {
		AuthenticationInfo authenticationInfo= secureStorageFacade.getStoredAuthenticationInfo();
		if (authenticationInfo != null) {
			return authenticationInfo;
		} else {
			return getUsernamePassword(Messages.WorkbenchPreferencePage_PluginName);
		}
	}

	@Override
	public void saveAuthenticationInfo(UIServices.AuthenticationInfo authenticationInfo) throws IOException {
		secureStorageFacade.saveAuthenticationInfo(authenticationInfo);
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

	@Override
	public String getRepositoryURL() {
		return Messages.Submitter_ProductionRepositoryURL;
	}

}
