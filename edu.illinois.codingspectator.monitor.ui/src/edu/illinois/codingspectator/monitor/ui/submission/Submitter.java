/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.monitor.ui.submission;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.p2.core.UIServices.AuthenticationInfo;
import org.eclipse.equinox.security.storage.StorageException;
import org.tmatesoft.svn.core.SVNAuthenticationException;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNException;

import edu.illinois.codingspectator.monitor.core.authentication.AuthenticationProvider;
import edu.illinois.codingspectator.monitor.core.submission.SVNManager;
import edu.illinois.codingspectator.monitor.core.submission.SubmitterListener;
import edu.illinois.codingspectator.monitor.core.submission.URLManager;
import edu.illinois.codingspectator.monitor.ui.Activator;
import edu.illinois.codingspectator.monitor.ui.AuthenticationPrompter;
import edu.illinois.codingspectator.monitor.ui.prefs.PrefsFacade;

/**
 * 
 * A Submitter is responsible for submitting the recorded refactoring logs. It gathers those logs
 * from a directory, imports it to some repository.
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class Submitter {

	public static final String LTK_BUNDLE_NAME= "org.eclipse.ltk.core.refactoring";

	public static final String WATCHED_DIRECTORY= Platform.getStateLocation(
			Platform.getBundle(LTK_BUNDLE_NAME)).toOSString();

	private SVNManager svnManager;

	private AuthenticationProvider authenticationProvider;

	public Submitter() {

	}

	public Submitter(AuthenticationProvider provider) {
		this.authenticationProvider= provider;
	}

	public void authenticateAndInitialize() throws InitializationException,
			FailedAuthenticationException, CanceledDialogException {
		try {
			AuthenticationProvider prompter= getAuthenticationPrompterLazily();
			AuthenticationInfo authenticationInfo= prompter.findUsernamePassword();

			if (isCanceled(authenticationInfo))
				throw new CanceledDialogException();

			URLManager urlManager= new URLManager(prompter.getRepositoryURL(), authenticationInfo.getUserName(), PrefsFacade
					.getInstance().getAndSetUUIDLazily());
			svnManager= new SVNManager(urlManager, WATCHED_DIRECTORY, authenticationInfo.getUserName(),
					authenticationInfo.getPassword());
			svnManager.doImport();
			svnManager.doCheckout();
			prompter.saveAuthenticationInfo(authenticationInfo);
		} catch (SVNAuthenticationException e) {
			throw new FailedAuthenticationException(e);
		} catch (SVNException e) {
			throw new InitializationException(e);
		} catch (StorageException e) {
			throw new InitializationException(e);
		} catch (IOException e) {
			throw new InitializationException(e);
		}

	}

	private boolean isCanceled(AuthenticationInfo authenticationInfo) {
		return authenticationInfo == null;
	}

	private AuthenticationProvider getAuthenticationPrompterLazily() {
		if (authenticationProvider == null) {
			authenticationProvider= new AuthenticationPrompter();
		}
		return authenticationProvider;
	}

	public void submit() throws SubmissionException {
		try {
			svnManager.doAdd(WATCHED_DIRECTORY);
			notifyPreSubmit();
			svnManager.doCommit(WATCHED_DIRECTORY);
			notifyPostSubmit();
		} catch (SVNException e) {
			throw new SubmissionException(e);
		}
	}

	private Collection<SubmitterListener> lookupExtensions() {
		IConfigurationElement[] config= Platform.getExtensionRegistry().getConfigurationElementsFor("edu.illinois.codingspectator.monitor.core.submitter");
		Collection<SubmitterListener> submitterListeners= new ArrayList<SubmitterListener>();
		try {
			for (IConfigurationElement e : config) {
				Object o= e.createExecutableExtension("class");
				submitterListeners.add((SubmitterListener)o);
			}
		} catch (CoreException e) {
			Activator.getDefault().createErrorStatus("Failed to lookup extensions for edu.illinois.codingspectator.monitor.core.submitter.", e);
		}
		return submitterListeners;
	}

	private void notifyPreSubmit() {
		Collection<SubmitterListener> submitterListeners= lookupExtensions();
		for (SubmitterListener submitterListener : submitterListeners) {
			submitterListener.preSubmit();
		}
	}

	private void notifyPostSubmit() {
		Collection<SubmitterListener> submitterListeners= lookupExtensions();
		for (SubmitterListener submitterListener : submitterListeners) {
			submitterListener.postSubmit();
		}
	}

	/**
	 * @return true if it can obtain a valid credential or false if the user has forcibly canceled
	 * @throws InitializationException
	 */
	public boolean promptUntilValidCredentialsOrCanceled()
			throws InitializationException {
		while (true) {
			try {
				authenticateAndInitialize();
			} catch (FailedAuthenticationException authEx) {
				try {
					getAuthenticationPrompterLazily().clearSecureStorage();
				} catch (IOException ioEx) {
					throw new InitializationException(ioEx);
				}
				continue;
			} catch (CanceledDialogException e) {
				return false;
			} catch (InitializationException initException) {
				if (initException.isLockedDirectoryError()) {
					tryCleanup();
				} else {
					throw initException;
				}
			}
			break;
		}
		return true;
	}

	protected void tryCleanup() throws InitializationException {
		try {
			SVNManager svnManager= new SVNManager(WATCHED_DIRECTORY);
			svnManager.doCleanup();
		} catch (SVNException e) {
			throw new InitializationException(e);
		}
	}

	@SuppressWarnings("serial")
	public static abstract class SubmitterException extends Exception {

		public SubmitterException() {
			super();
		}

		public SubmitterException(Exception e) {
			super(e);
		}
	}

	@SuppressWarnings("serial")
	public static class FailedAuthenticationException extends
			SubmitterException {

		public FailedAuthenticationException() {
			super();
		}

		public FailedAuthenticationException(Exception e) {
			super(e);
		}
	}

	@SuppressWarnings("serial")
	public static class CanceledDialogException extends SubmitterException {

		public CanceledDialogException() {
			super();
		}

		public CanceledDialogException(Exception e) {
			super(e);
		}
	}

	@SuppressWarnings("serial")
	public static class InitializationException extends SubmitterException {

		private SVNErrorCode errorCode;

		public InitializationException(Exception e) {
			super(e);
		}

		public InitializationException(SVNException e) {
			super(e);
			errorCode= e.getErrorMessage().getErrorCode();
		}

		public boolean isLockedDirectoryError() {
			if (errorCode != null) {
				return errorCode.equals(SVNErrorCode.WC_LOCKED);
			} else {
				return false;
			}
		}
	}

	@SuppressWarnings("serial")
	public static class SubmissionException extends SubmitterException {

		public SubmissionException(Exception e) {
			super(e);
		}

	}

}
