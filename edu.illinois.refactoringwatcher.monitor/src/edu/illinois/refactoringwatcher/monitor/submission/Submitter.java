package edu.illinois.refactoringwatcher.monitor.submission;

import org.eclipse.core.runtime.Platform;
import org.tmatesoft.svn.core.SVNException;

import edu.illinois.refactoringwatcher.monitor.Messages;
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

	private static String getRepositoryOffsetURL() {
		return joinByURLSeparator(PrefsFacade.getNetid(), PrefsFacade.getUUID());
	}

	private static String joinByURLSeparator(String... strings) {
		StringBuilder sb= new StringBuilder();
		for (int i= 0; i < strings.length; ++i) {
			sb.append(strings[i]);
			sb.append("/"); //$NON-NLS-1$
		}
		return sb.toString();
	}

	public Submitter(String username, String password) {
		this.svnManager= new SVNManager(repositoryBaseURL, username, password);
	}

	/**
	 * @idempotent
	 */
	public void initialize() throws InitializationException {
		try {
			svnManager.doImport(watchedDirectory, getRepositoryOffsetURL());
			svnManager.doCheckout(watchedDirectory, getRepositoryOffsetURL());
		} catch (SVNException e) {
			throw new InitializationException(e);
		}
	}

	@SuppressWarnings("serial")
	public static class InitializationException extends Exception {

		public InitializationException(SVNException e) {
			super(e);
		}

	}

}
