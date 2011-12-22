/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.monitor.core.submission;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;

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

	private final String UUID;

	public URLManager(String repositoryBaseURL, String username, String UUID) {
		super();
		this.repositoryBaseURL= repositoryBaseURL;
		this.username= username;
		this.UUID= UUID;
	}

	public SVNURL getSVNURL(String url) throws SVNException {
		return SVNURL.parseURIEncoded(url);
	}

	public SVNURL getPersonalSVNURL() throws SVNException {
		return getSVNURL(getPersonalURL());
	}

	private String getPersonalURL() {
		return joinByURLSeparator(repositoryBaseURL, username);
	}

	public SVNURL getPersonalWorkspaceSVNURL() throws SVNException {
		return getSVNURL(getPersonalWorkspaceURL());
	}

	public String getPersonalWorkspaceURL() {
		return joinByURLSeparator(getPersonalURL(), UUID);
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
