/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.monitor.core.submission;

import java.io.File;

import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNRevision;

/**
 * Remote (+local through inheritance) operations for SVNManager. This uses inheritance for code
 * reuse rather than a strict is-a relationship.
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class RemoteSVNManager extends LocalSVNManager {

	protected RemoteSVNManager(URLManager urlManager, String svnWorkingCopyDirectory, String username, String password) {
		super(urlManager, svnWorkingCopyDirectory, username, password);
	}

	public void doImport() throws SVNException {
		if (isWorkingDirectoryValid())
			return;
		cm.getCommitClient().doImport(svnWorkingCopyDirectory, urlManager.getPersonalRepositorySVNURL(), "Initial import", null, false, true, SVNDepth.INFINITY);
	}

	public void doCheckout() throws SVNException {
		cm.getUpdateClient().doCheckout(urlManager.getPersonalRepositorySVNURL(), svnWorkingCopyDirectory, SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.INFINITY,
				true);
	}

	public void doCommit() throws SVNException {
		File[] pathToCommitFiles= new File[] { svnWorkingCopyDirectory };
		cm.getCommitClient().doCommit(pathToCommitFiles, false, COMMIT_MESSAGE, null, null, false, true, SVNDepth.INFINITY);
	}
}
