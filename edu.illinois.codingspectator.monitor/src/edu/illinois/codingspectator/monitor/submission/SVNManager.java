/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.monitor.submission;

import java.io.File;

import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNRevision;

import edu.illinois.codingspectator.monitor.Activator;

/**
 * This is the concrete implementation of the {@link Submitter} design contract for an SVN backend.
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class SVNManager {

	private static final String COMMIT_MESSAGE= Activator.PLUGIN_ID;

	private final SVNClientManager cm;

	private final URLManager urlManager;

	private String svnWorkingCopyDirectory;

	public SVNManager(URLManager urlManager, String svnWorkingCopyDirectory, String username, String password) {
		this.urlManager= urlManager;
		this.svnWorkingCopyDirectory= svnWorkingCopyDirectory;
		setupLibrary();
		cm= SVNClientManager.newInstance(null, username, password);
	}

	/**
	 * Lightweight version for local working-copy operations only. No authentication info is
	 * required.
	 */
	public SVNManager(String svnWorkingCopyDirectory) {
		this(null, svnWorkingCopyDirectory, null, null);
	}

	//
	// Local working copy operations
	//

	public SVNInfo doInfo() throws SVNException {
		File workingCopyDirectory= new File(svnWorkingCopyDirectory);
		return cm.getWCClient().doInfo(workingCopyDirectory, SVNRevision.WORKING);
	}

	/**
	 * @return the username of the local working copy, or the empty string if the local working copy
	 *         has not been created yet.
	 */
	public String getSVNWorkingCopyUsername() {
		try {
			SVNInfo info= doInfo();
			return info.getAuthor();
		} catch (SVNException e) {
			// Do not log. This is a harmless operation. If nothing is available, we just default to ""
			return "";
		}
	}

	// 
	// Remote repository operations
	//

	public void doImport() throws SVNException {
		if (isWorkingDirectoryValid())
			return;
		File file= new File(svnWorkingCopyDirectory);
		cm.getCommitClient().doImport(file, urlManager.getPersonalRepositorySVNURL(), "Initial import", null, false, true, SVNDepth.INFINITY);
	}

	public void doCheckout() throws SVNException {
		File destinationPathFile= new File(svnWorkingCopyDirectory);
		cm.getUpdateClient().doCheckout(urlManager.getPersonalRepositorySVNURL(), destinationPathFile, SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.INFINITY,
				true);
	}

	public void doAdd(String pathToAdd) throws SVNException {
		File pathToAddFile= new File(pathToAdd);
		cm.getWCClient().doAdd(pathToAddFile, true, false, false, SVNDepth.INFINITY, false, false);
	}

	public void doCommit(String pathToCommit) throws SVNException {
		File[] pathToCommitFiles= new File[] { new File(pathToCommit) };
		cm.getCommitClient().doCommit(pathToCommitFiles, false, COMMIT_MESSAGE, null, null, false, true, SVNDepth.INFINITY);
	}

	public boolean isWorkingDirectoryValid() {
		File file= new File(svnWorkingCopyDirectory);
		try {
			cm.getWCClient().doInfo(file, SVNRevision.WORKING);
		} catch (SVNException e) {
			return false;
		}
		return true;
	}

	private static void setupLibrary() {
		//For using over http:// and https://
		DAVRepositoryFactory.setup();
		//For using over svn:// and svn+xxx://
		SVNRepositoryFactoryImpl.setup();
		//For using over file:///
		FSRepositoryFactory.setup();
	}

}
