package edu.illinois.refactoringwatcher.monitor.submission;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.p2.core.UIServices.AuthenticationInfo;
import org.tmatesoft.svn.core.SVNAuthenticationException;
import org.tmatesoft.svn.core.SVNException;

import edu.illinois.refactoringwatcher.monitor.Messages;
import edu.illinois.refactoringwatcher.monitor.authentication.AuthenticationPrompter;
import edu.illinois.refactoringwatcher.monitor.authentication.AuthenticationProvider;
import edu.illinois.refactoringwatcher.monitor.prefs.PrefsFacade;

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

	private static final String repositoryBaseURL= Messages.Submitter_repository_base_url;

	public static final String watchedDirectory= Platform.getStateLocation(Platform.getBundle(Messages.Submitter_ltk_bundle_name)).toOSString();

	private SVNManager svnManager;

	private AuthenticationProvider authenticationProvider;

	public Submitter() {

	}

	public Submitter(AuthenticationProvider provider) {
		this.authenticationProvider= provider;
	}

	private static String getRepositoryOffsetURL(String username) {
		return joinByURLSeparator(username, PrefsFacade.getUUID());
	}

	private static String joinByURLSeparator(String... strings) {
		StringBuilder sb= new StringBuilder();
		for (int i= 0; i < strings.length; ++i) {
			sb.append(strings[i]);
			sb.append("/"); //$NON-NLS-1$
		}
		return sb.toString();
	}

	/**
	 * @throws AuthenticationException
	 * @idempotent
	 */
	public void initialize() throws InitializationException, AuthenticationException {
		try {
			AuthenticationProvider prompter= getAuthenticationPrompter();
			AuthenticationInfo authenticationInfo= prompter.findUsernamePassword();
			if (authenticationInfo == null) {
				throw new AuthenticationException("No username or password were provided.");
			}
			svnManager= new SVNManager(repositoryBaseURL, authenticationInfo.getUserName(), authenticationInfo.getPassword());
			svnManager.doImport(watchedDirectory, getRepositoryOffsetURL(authenticationInfo.getUserName()));
			svnManager.doCheckout(watchedDirectory, getRepositoryOffsetURL(authenticationInfo.getUserName()));
		} catch (SVNAuthenticationException e) {
			throw new AuthenticationException(e);
		} catch (SVNException e) {
			throw new InitializationException(e);
		}

	}

	private AuthenticationProvider getAuthenticationPrompter() {
		if (authenticationProvider == null)
			return new AuthenticationPrompter();
		else
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

	public void upload() throws InitializationException, SubmissionException, AuthenticationException {
		initialize();
		submit();
	}

	@SuppressWarnings("serial")
	public static class SubmitterException extends Exception {

		public SubmitterException(SVNException e) {
			super(e);
		}

		public SubmitterException(String message) {
			super(message);
		}
	}

	@SuppressWarnings("serial")
	public static class AuthenticationException extends SubmitterException {

		public AuthenticationException(SVNException e) {
			super(e);
		}

		public AuthenticationException(String message) {
			super(message);
		}

	}

	@SuppressWarnings("serial")
	public static class InitializationException extends SubmitterException {

		public InitializationException(SVNException e) {
			super(e);
		}

	}

	@SuppressWarnings("serial")
	public static class SubmissionException extends SubmitterException {

		public SubmissionException(SVNException e) {
			super(e);
		}

	}

}
