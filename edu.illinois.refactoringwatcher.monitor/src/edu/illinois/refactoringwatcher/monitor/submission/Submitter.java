package edu.illinois.refactoringwatcher.monitor.submission;

import java.io.IOException;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.p2.core.UIServices.AuthenticationInfo;
import org.eclipse.equinox.security.storage.StorageException;
import org.tmatesoft.svn.core.SVNAuthenticationException;
import org.tmatesoft.svn.core.SVNException;

import edu.illinois.refactoringwatcher.monitor.Messages;
import edu.illinois.refactoringwatcher.monitor.authentication.AuthenticationProvider;
import edu.illinois.refactoringwatcher.monitor.ui.AuthenticationPrompter;

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

	public static final String watchedDirectory= Platform.getStateLocation(Platform.getBundle(Messages.Submitter_LTKBundleName)).toOSString();

	private SVNManager svnManager;

	private AuthenticationProvider authenticationProvider;

	public Submitter() {

	}

	public Submitter(AuthenticationProvider provider) {
		this.authenticationProvider= provider;
	}

	public static String getFeatureVersion() {
		return Platform.getBundle(Messages.Submitter_FeatureBundleName).getVersion().toString();
	}

	public void authenticateAndInitialize() throws InitializationException, FailedAuthenticationException, NoAuthenticationInformationFoundException {
		try {
			AuthenticationProvider prompter= getAuthenticationPrompterLazily();
			AuthenticationInfo authenticationInfo= prompter.findUsernamePassword();
			if (authenticationInfo == null) {
				throw new NoAuthenticationInformationFoundException();
			}
			svnManager= new SVNManager(new URLManager(Messages.Submitter_RepositoryBaseURL, authenticationInfo.getUserName(), getFeatureVersion()), authenticationInfo.getUserName(),
					authenticationInfo.getPassword());
			svnManager.doImport(watchedDirectory);
			svnManager.doCheckout(watchedDirectory);
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

	private AuthenticationProvider getAuthenticationPrompterLazily() {
		if (authenticationProvider == null) {
			authenticationProvider= new AuthenticationPrompter();
		}
		return authenticationProvider;
	}

	public void submit() throws SubmissionException {
		try {
			svnManager.doAdd(watchedDirectory);
			svnManager.doCommit(watchedDirectory);
		} catch (SVNException e) {
			throw new SubmissionException(e);
		}
	}

	public void initializeUntilValidCredentials() throws InitializationException {
		while (true) {
			try {
				authenticateAndInitialize();
			} catch (NoAuthenticationInformationFoundException noAuthEx) {
				continue;
			} catch (FailedAuthenticationException authEx) {
				try {
					getAuthenticationPrompterLazily().clearSecureStorage();
				} catch (IOException ioEx) {
					throw new InitializationException(ioEx);
				}
				continue;
			}
			break;
		}
	}

	@SuppressWarnings("serial")
	public static class SubmitterException extends Exception {

		public SubmitterException() {
			super();
		}

		public SubmitterException(Exception e) {
			super(e);
		}

		public SubmitterException(String message) {
			super(message);
		}
	}

	@SuppressWarnings("serial")
	public static class FailedAuthenticationException extends SubmitterException {

		public FailedAuthenticationException() {
			super();
		}

		public FailedAuthenticationException(Exception e) {
			super(e);
		}

		public FailedAuthenticationException(String message) {
			super(message);
		}

	}

	@SuppressWarnings("serial")
	public static class NoAuthenticationInformationFoundException extends SubmitterException {

		public NoAuthenticationInformationFoundException() {
			super();
		}

		public NoAuthenticationInformationFoundException(Exception e) {
			super(e);
		}

		public NoAuthenticationInformationFoundException(String message) {
			super(message);
		}

	}

	@SuppressWarnings("serial")
	public static class InitializationException extends SubmitterException {

		public InitializationException(Exception e) {
			super(e);
		}

	}

	@SuppressWarnings("serial")
	public static class SubmissionException extends SubmitterException {

		public SubmissionException(Exception e) {
			super(e);
		}

	}

}
