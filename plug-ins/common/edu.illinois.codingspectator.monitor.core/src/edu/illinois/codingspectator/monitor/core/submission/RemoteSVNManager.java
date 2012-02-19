/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.monitor.core.submission;

import java.io.File;

import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNRevision;

import edu.illinois.codingspectator.monitor.core.Activator;

/**
 * Implements remote Subversion operations.
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class RemoteSVNManager extends AbstractSVNManager {

	protected static final String COMMIT_MESSAGE= Activator.PLUGIN_ID;

	protected final SVNClientManager cm;

	protected final URLManager urlManager;

	protected RemoteSVNManager(URLManager urlManager, String svnWorkingCopyDirectory, String username, String password) {
		super(svnWorkingCopyDirectory);
		this.urlManager= urlManager;
		setupLibrary();
		cm= SVNClientManager.newInstance(null, username, password);
	}

	private static void setupLibrary() {
		//For using over http:// and https://
		DAVRepositoryFactory.setup();
		//For using over svn:// and svn+xxx://
		SVNRepositoryFactoryImpl.setup();
		//For using over file:///
		FSRepositoryFactory.setup();
	}

	public void doImport() throws SVNException {
		cm.getCommitClient().doImport(svnWorkingCopyDirectory, urlManager.getPersonalWorkspaceSVNURL(), "Initial import", null, false, true, SVNDepth.INFINITY);
	}

	public void doCheckout() throws SVNException {
		cm.getUpdateClient().doCheckout(urlManager.getPersonalWorkspaceSVNURL(), svnWorkingCopyDirectory, SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.INFINITY,
				true);
	}

	public void doUpdate() throws SVNException {
		cm.getUpdateClient().doUpdate(svnWorkingCopyDirectory, SVNRevision.HEAD, SVNDepth.INFINITY, true, true);
	}

	public void doCommit() throws SVNException {
		File[] pathToCommitFiles= new File[] { svnWorkingCopyDirectory };
		cm.getCommitClient().doCommit(pathToCommitFiles, false, COMMIT_MESSAGE, null, null, false, true, SVNDepth.INFINITY);
	}

	public long getCommittedRevisionNumber() throws SVNException {
		return doInfo(urlManager.getPersonalWorkspaceSVNURL()).getCommittedRevision().getNumber();
	}

	private long getRevisionNumber() throws SVNException {
		return doInfo(urlManager.getPersonalWorkspaceSVNURL()).getRevision().getNumber();
	}

	private SVNInfo doInfo(SVNURL svnURL) throws SVNException {
		return cm.getWCClient().doInfo(svnURL, SVNRevision.HEAD, SVNRevision.HEAD);
	}

	public void doDelete(String commitMessage) throws SVNException {
		cm.getCommitClient().doDelete(new SVNURL[] { urlManager.getPersonalWorkspaceSVNURL() }, commitMessage);
	}

	public boolean isWatchedFolderInRepository() {
		try {
			getRevisionNumber();
		} catch (SVNException e) {
			return false;
		}
		return true;
	}

	public boolean isAuthenticationInformationValid() {
		try {
			doInfo(urlManager.getPersonalSVNURL());
		} catch (SVNException e) {
			return false;
		}
		return true;
	}

}
