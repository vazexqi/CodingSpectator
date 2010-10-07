package edu.illinois.refactoringwatcher.monitor.submission;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;

import edu.illinois.refactoringwatcher.monitor.prefs.PrefsFacade;

/**
 * This class is responsible for computing the URLs for the remote repository and local working
 * directory.
 * 
 * @author Mohsen Vakilian
 * 
 */
public class URLManager {

	private final String repositoryBaseURL;

	private final String username;

	private final String featureVersion;

	public URLManager(String repositoryBaseURL, String username, String featureVersion) {
		super();
		this.repositoryBaseURL= repositoryBaseURL;
		this.username= username;
		this.featureVersion= featureVersion;
	}

	public SVNURL getSVNURL(String url) throws SVNException {
		return SVNURL.parseURIEncoded(url);
	}

	public SVNURL getPersonalRepositorySVNURL() throws SVNException {
		return getSVNURL(getPersonalRepositoryURL());
	}

	public String getPersonalRepositoryURL() {
		return joinByURLSeparator(repositoryBaseURL, getRepositoryOffsetURL());
	}

	private String getRepositoryOffsetURL() {
		return joinByURLSeparator(username, PrefsFacade.getInstance().getAndSetUUIDLazily(), featureVersion);
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
