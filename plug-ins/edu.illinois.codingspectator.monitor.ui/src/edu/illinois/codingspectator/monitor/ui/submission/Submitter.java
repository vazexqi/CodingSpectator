/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.monitor.ui.submission;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.equinox.p2.core.UIServices.AuthenticationInfo;
import org.eclipse.equinox.security.storage.StorageException;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNException;

import edu.illinois.codingspectator.data.CodingSpectatorDataPlugin;
import edu.illinois.codingspectator.monitor.core.authentication.AuthenticationProvider;
import edu.illinois.codingspectator.monitor.core.submission.LocalSVNManager;
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

	public static final String WATCHED_DIRECTORY= CodingSpectatorDataPlugin.getStorageLocation().toOSString();

	private SVNManager svnManager;

	private AuthenticationProvider authenticationProvider;

	private Collection<SubmitterListener> submitterListeners= new ArrayList<SubmitterListener>();

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
			svnManager= new SVNManager(urlManager, WATCHED_DIRECTORY, authenticationInfo.getUserName(), authenticationInfo.getPassword());
			prompter.saveAuthenticationInfo(authenticationInfo);
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
		boolean submissionSucceeded= false;

		try {
			submitterListeners= lookupExtensions();
			notifyPreSubmit();
			resolveLocalAndRemoteDataMismatches();
			svnManager.doImportIfNecessary();
			svnManager.doCheckout();
			svnManager.doAdd();
			notifyPreCommit();
			svnManager.doCommit();
			updateLocalRevisionNumbers();
			submissionSucceeded= true;
		} catch (Throwable e) {
			throw new SubmissionException(e);
		} finally {
			notifyPostSubmit(submissionSucceeded);
		}
	}

	private void resolveLocalAndRemoteDataMismatches() throws SVNException, CoreException {
		final String svnDeleteMessage= "Deleted workspace data because of an outdated SVN working copy.";
		if (svnManager.isWatchedFolderInRepository()) {
			if (!svnManager.isWorkingDirectoryValid()) {
				svnManager.doDelete(svnDeleteMessage);
			} else if (svnManager.isLocalWorkCopyOutdated()) {
				svnManager.removeSVNMetaData();
				svnManager.doDelete(svnDeleteMessage);
			}
		} else {
			svnManager.removeSVNMetaData();
		}
	}

	private void updateLocalRevisionNumbers() throws SVNException {
		svnManager.doUpdate();
	}

	private Collection<SubmitterListener> lookupExtensions() {
		String extensionPointId= "edu.illinois.codingspectator.monitor.core.submitter";

		IConfigurationElement[] config= Platform.getExtensionRegistry().getConfigurationElementsFor(extensionPointId);
		Collection<SubmitterListener> submitterListeners= new ArrayList<SubmitterListener>();
		try {
			for (IConfigurationElement e : config) {
				Object o= e.createExecutableExtension("class");
				submitterListeners.add((SubmitterListener)o);
			}
		} catch (CoreException e) {
			Activator.getDefault().createErrorStatus(String.format("Failed to lookup extensions for %s.", extensionPointId), e);
		}
		return submitterListeners;
	}

	private void notifyPreSubmit() {
		for (final SubmitterListener submitterListener : submitterListeners) {
			SafeRunner.run(new ISafeRunnable() {

				@Override
				public void run() throws Exception {
					submitterListener.preSubmit();
				}

				@Override
				public void handleException(Throwable exception) {
				}
			});
		}
	}

	private void notifyPreCommit() {
		for (final SubmitterListener submitterListener : submitterListeners) {
			SafeRunner.run(new ISafeRunnable() {

				@Override
				public void run() throws Exception {
					submitterListener.preCommit();
				}

				@Override
				public void handleException(Throwable exception) {
				}
			});
		}
	}

	private void notifyPostSubmit(final boolean succeeded) {
		for (final SubmitterListener submitterListener : submitterListeners) {
			SafeRunner.run(new ISafeRunnable() {

				@Override
				public void run() throws Exception {
					submitterListener.postSubmit(succeeded);
				}

				@Override
				public void handleException(Throwable exception) {
				}
			});
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
			LocalSVNManager svnManager= new LocalSVNManager(WATCHED_DIRECTORY);
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

		public SubmitterException(Throwable e) {
			super(e);
		}
	}

	@SuppressWarnings("serial")
	public static class FailedAuthenticationException extends
			SubmitterException {

		public FailedAuthenticationException() {
			super();
		}

		public FailedAuthenticationException(Throwable e) {
			super(e);
		}
	}

	@SuppressWarnings("serial")
	public static class CanceledDialogException extends SubmitterException {

		public CanceledDialogException() {
			super();
		}

		public CanceledDialogException(Throwable e) {
			super(e);
		}
	}

	@SuppressWarnings("serial")
	public static class InitializationException extends SubmitterException {

		private SVNErrorCode errorCode;

		public InitializationException(Throwable e) {
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

		public SubmissionException(Throwable e) {
			super(e);
		}

	}

}
