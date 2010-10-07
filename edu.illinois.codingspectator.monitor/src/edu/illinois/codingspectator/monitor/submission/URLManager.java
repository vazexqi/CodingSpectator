package edu.illinois.codingspectator.monitor.submission;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;

import edu.illinois.codingspectator.monitor.prefs.PrefsFacade;

/**
 * This class is responsible for computing the URLs for the remote repository and local working
 * directory.
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class URLManager {

	private final String repositoryBaseURL;

	private final String username;

	public URLManager(String repositoryBaseURL, String username) {
		super();
		this.repositoryBaseURL= repositoryBaseURL;
		this.username= username;
	}

	public SVNURL getSVNURL(String url) throws SVNException {
		return SVNURL.parseURIEncoded(url);
	}

	public SVNURL getPersonalRepositorySVNURL() throws SVNException {
		return getSVNURL(getPersonalRepositoryURL());
	}

	public String getPersonalRepositoryURL() {
		return joinByURLSeparator(repositoryBaseURL, username, PrefsFacade
				.getInstance().getAndSetUUIDLazily());
	}

	public String joinByURLSeparator(final String... strings) {
		StringBuilder sb= new StringBuilder();
		for (int i= 0; i < strings.length; ++i) {
			sb.append(strings[i]);
			sb.append("/"); //$NON-NLS-1$
		}
		return sb.toString();
	}

}
