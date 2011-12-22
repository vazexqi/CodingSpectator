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
import org.tmatesoft.svn.core.SVNException;

import edu.illinois.codingspectator.data.CodingSpectatorDataPlugin;
import edu.illinois.codingspectator.monitor.core.authentication.AuthenticationProvider;
import edu.illinois.codingspectator.monitor.core.submission.SVNManager;
import edu.illinois.codingspectator.monitor.core.submission.SubmitterListener;
import edu.illinois.codingspectator.monitor.core.submission.URLManager;
import edu.illinois.codingspectator.monitor.ui.Activator;
import edu.illinois.codingspectator.monitor.ui.AuthenticationPrompter;
import edu.illinois.codingspectator.monitor.ui.ExceptionUtil;
import edu.illinois.codingspectator.monitor.ui.prefs.PrefsFacade;

/**
 * 
 * A Submitter is responsible for submitting the recorded refactoring logs. It gathers those logs
 * from a directory, imports it to some repository.
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * @author Stas Negara
 * 
 */
public class Submitter {

	public static final String WATCHED_FOLDER= CodingSpectatorDataPlugin.getStorageLocation().toOSString();

	private SVNManager svnManager;

	private AuthenticationProvider authenticationProvider;

	private Collection<SubmitterListener> submitterListeners= new ArrayList<SubmitterListener>();

	private String uuid;

	public Submitter() {

	}

	public Submitter(AuthenticationProvider provider) {
		this.authenticationProvider= provider;
	}

	public enum AuthenticanResult {
		CANCELED_AUTHENTICATION, WRONG_AUTHENTICATION, OK
	}

	public String getUUID() {
		return uuid;
	}

	public AuthenticanResult authenticate() throws InitializationException {
		AuthenticationProvider prompter= getAuthenticationPrompterLazily();
		AuthenticationInfo authenticationInfo= prompter.findUsernamePassword();

		if (isCanceled(authenticationInfo)) {
			return AuthenticanResult.CANCELED_AUTHENTICATION;
		}

		uuid= PrefsFacade.getInstance().getAndSetUUIDLazily();
		URLManager urlManager= new URLManager(prompter.getRepositoryURL(), authenticationInfo.getUserName(), uuid);
		svnManager= new SVNManager(urlManager, WATCHED_FOLDER, authenticationInfo.getUserName(), authenticationInfo.getPassword());
		if (!svnManager.isAuthenticationInformationValid()) {
			return AuthenticanResult.WRONG_AUTHENTICATION;
		}

		try {
			prompter.saveAuthenticationInfo(authenticationInfo);
		} catch (StorageException e) {
			throw new InitializationException(e);
		} catch (IOException e) {
			throw new InitializationException(e);
		}

		return AuthenticanResult.OK;
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
			doSVNSubmit();
			submissionSucceeded= true;
		} catch (Throwable e1) {
			try {
				removeLocalAndRemoteData("Deleted the remote data because the submission failed with the following exception:\n" + ExceptionUtil.getStackTrace(e1));
				doSVNSubmit();
				submissionSucceeded= true;
			} catch (Throwable e2) {
				throw new SubmissionException(e2);
			}
		} finally {
			notifyPostSubmit(submissionSucceeded);
		}
	}

	/**
	 * The following method is useful for testing purposes. This methods notifies all
	 * SubmitterListeners. Since some of the listeners transfer their data to the watched folder
	 * when they get notified, this method gathers all the collected data into the watched folder.
	 */
	public void notifyListeners() {
		submitterListeners= lookupExtensions();
		notifyPreSubmit();
		notifyPreCommit();
		notifyPostSubmit(true);
	}

	private void doSVNSubmit() throws SVNException {
		svnManager.doImportIfNecessary();
		svnManager.doCleanupIfPossible();
		svnManager.doCheckout();
		svnManager.doAdd();
		notifyPreCommit();
		svnManager.doCommit();
		updateLocalRevisionNumbers();
	}

	/**
	 * 
	 * This method is used by an automated test.
	 * 
	 * @return
	 * @throws SVNException
	 */
	public boolean doLocalAndRemoteDataMatch() throws SVNException {
		return svnManager.isWatchedFolderInRepository() && svnManager.isWorkingDirectoryValid() && !svnManager.isLocalWorkCopyOutdated();
	}

	private void resolveLocalAndRemoteDataMismatches() throws SVNException, CoreException {
		if (svnManager.isWatchedFolderInRepository()) {
			if (!svnManager.isWorkingDirectoryValid()) {
				removeRemoteData("Deleted the remote data because the personal worskpace existed on the remote repository, but, the local working copy was invalid.");
			} else if (svnManager.isLocalWorkCopyOutdated()) {
				removeLocalAndRemoteData("Deleted the remote data because the local working copy was outdated.");
			}
		} else {
			svnManager.removeSVNMetaData();
		}
	}

	private void removeRemoteData(String svnMessage) throws SVNException {
		svnManager.doDelete(svnMessage);
	}

	private void removeLocalAndRemoteData(String svnMessage) throws CoreException, SVNException {
		svnManager.removeSVNMetaData();
		removeRemoteData(svnMessage);
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
	public boolean promptUntilValidCredentialsOrCanceled() throws InitializationException {
		AuthenticanResult authenticanResult;
		do {
			authenticanResult= authenticate();
			if (authenticanResult == AuthenticanResult.CANCELED_AUTHENTICATION) {
				return false;
			}
		} while (authenticanResult != AuthenticanResult.OK);
		return true;
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
	public static class InitializationException extends SubmitterException {

		public InitializationException(Throwable e) {
			super(e);
		}

		public InitializationException(SVNException e) {
			super(e);
		}

	}

	@SuppressWarnings("serial")
	public static class SubmissionException extends SubmitterException {

		public SubmissionException(Throwable e) {
			super(e);
		}

	}

}
