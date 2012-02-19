/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.monitor.core.submission;

import org.eclipse.core.runtime.CoreException;
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

	public void doImportIfNecessary() throws SVNException {
		if (!localSVNManager.isWorkingDirectoryValid()) {
			remoteSVNManager.doImport();
		}
	}

	public void doCleanupIfPossible() throws SVNException {
		if (localSVNManager.isWorkingDirectoryValid()) {
			localSVNManager.doCleanup();
		}
	}

	public void doCheckout() throws SVNException {
		remoteSVNManager.doCheckout();
	}

	public void doUpdate() throws SVNException {
		remoteSVNManager.doUpdate();
	}

	public void doCommit() throws SVNException {
		remoteSVNManager.doCommit();
	}

	public void doAdd() throws SVNException {
		localSVNManager.doAdd();
	}

	public boolean isLocalWorkCopyOutdated() throws SVNException {
		return remoteSVNManager.getCommittedRevisionNumber() > localSVNManager.getRevisionNumber();
	}

	public void doDelete(String commitMessage) throws SVNException {
		remoteSVNManager.doDelete(commitMessage);
	}

	public void removeSVNMetaData() throws CoreException {
		localSVNManager.removeSVNMetaData();
	}

	public boolean isWorkingDirectoryValid() {
		return localSVNManager.isWorkingDirectoryValid();
	}

	public boolean isWatchedFolderInRepository() {
		return remoteSVNManager.isWatchedFolderInRepository();
	}

	public boolean isAuthenticationInformationValid() {
		return remoteSVNManager.isAuthenticationInformationValid();
	}

}
