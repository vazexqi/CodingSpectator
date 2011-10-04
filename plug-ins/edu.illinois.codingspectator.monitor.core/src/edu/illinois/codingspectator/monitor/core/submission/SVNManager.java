/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.monitor.core.submission;

import org.tmatesoft.svn.core.SVNException;

/**
 * A facade over local and remote Subversion operations.
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class SVNManager {

	LocalSVNManager localSVNManager;

	RemoteSVNManager remoteSVNManager;

	public SVNManager(URLManager urlManager, String svnWorkingCopyDirectory, String username, String password) {
		localSVNManager= new LocalSVNManager(svnWorkingCopyDirectory);
		remoteSVNManager= new RemoteSVNManager(urlManager, svnWorkingCopyDirectory, username, password);
	}

	public void doImport() throws SVNException {
		if (localSVNManager.isWorkingDirectoryValid())
			return;
		remoteSVNManager.doImport();
	}

	public void doCheckout() throws SVNException {
		remoteSVNManager.doCheckout();
	}

	public void doCommit() throws SVNException {
		remoteSVNManager.doCommit();
	}

	public void doAdd() throws SVNException {
		localSVNManager.doAdd();
	}

	@Deprecated
	public void doResolve() throws SVNException {
		localSVNManager.doResolve();
	}

	public boolean isLocalWorkCopyOutdated() throws SVNException {
		return remoteSVNManager.hasIncomingChange();
	}

}
