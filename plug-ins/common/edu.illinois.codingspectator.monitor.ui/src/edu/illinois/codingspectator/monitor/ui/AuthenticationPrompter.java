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
import org.eclipse.ltk.core.refactoring.codingspectator.RunningModes;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import edu.illinois.codingspectator.monitor.core.authentication.AuthenticationProvider;
import edu.illinois.codingspectator.monitor.core.submission.LocalSVNManager;
import edu.illinois.codingspectator.monitor.ui.prefs.SecureStorageFacade;
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

		// Opens the dialog using syncExec because the result of the dialog is accessed synchronously later.
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
				String username= new LocalSVNManager(Submitter.WATCHED_FOLDER).getSVNWorkingCopyUsername();
				UserValidationDialog dialog= new UserValidationDialog(shell, dialogTitle, dialogState.getDialogDescription(), username, dialogState.getDialogType());
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


	private AuthenticationInfo lookupCredentialsOrAsk() throws IOException {
		AuthenticationInfo authenticationInfo= secureStorageFacade.getStoredAuthenticationInfo();
		if (authenticationInfo != null) {
			return authenticationInfo;
		} else {
			return getUsernamePassword(Messages.PluginName);
		}
	}

	@Override
	public void saveAuthenticationInfo(UIServices.AuthenticationInfo authenticationInfo) throws IOException {
		secureStorageFacade.saveAuthenticationInfo(authenticationInfo);
	}

	@Override
	public AuthenticationInfo findUsernamePassword() {
		try {
			return lookupCredentialsOrAsk();
		} catch (Exception ex) {
			Status errorStatus= Activator.getDefault().createErrorStatus(Messages.AuthenticationPrompter_FailureMessage, ex);
			Activator.getDefault().log(errorStatus);
		}
		return null;
	}

	@Override
	public String getRepositoryURL() {
		if (RunningModes.isInProductionMode()) {
			return Messages.AuthenticationPrompter_ProductionRepositoryURL;
		} else {
			return Messages.AuthenticationPrompter_TestRepositoryURL;
		}
	}
}
