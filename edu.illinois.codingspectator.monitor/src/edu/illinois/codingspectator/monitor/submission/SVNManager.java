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

import edu.illinois.codingspectator.codingtracker.Logger;
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

	public SVNManager(URLManager urlManager, String username, String password) {
		this.urlManager= urlManager;
		setupLibrary();
		cm= SVNClientManager.newInstance(null, username, password);
	}

	/**
	 * Lightweight version for local working-copy operations only. No authentication info is
	 * required.
	 */
	public SVNManager() {
		this.urlManager= null;
		setupLibrary();
		cm= SVNClientManager.newInstance();
	}

	//
	// Local working copy operations
	//

	public SVNInfo doInfo(String directory) throws SVNException {
		File workingCopyDirectory= new File(directory);
		return cm.getWCClient().doInfo(workingCopyDirectory, SVNRevision.WORKING);
	}

	// 
	// Remote repository operations
	//

	public void doImport(String directory) throws SVNException {
		if (isWorkingDirectoryValid(directory))
			return;
		File file= new File(directory);
		cm.getCommitClient().doImport(file, urlManager.getPersonalRepositorySVNURL(), "Initial import", null, false, true, SVNDepth.INFINITY);
	}

	public void doCheckout(String destinationPath) throws SVNException {
		File destinationPathFile= new File(destinationPath);
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

	public boolean isWorkingDirectoryValid(String directory) {
		File file= new File(directory);
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
