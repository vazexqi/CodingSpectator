/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.monitor.core.submission;

import java.io.File;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;

import edu.illinois.codingspectator.monitor.core.Activator;

/**
 * This is the implementation of the {@link Submitter} design contract for an SVN backend. There are
 * two versions of the backend: LocalSVNManager and RemoteSVNManager. LocalSVNManager only allows
 * operations that do not require communicating with the server. RemoteSVNManager allows both local
 * operations and operations that require communication with the server (thus it requires login
 * credentials).
 * 
 * This is the factory for creating LocalSVNManager and RemoteSVNManager.
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public abstract class SVNManager {

	protected static final String COMMIT_MESSAGE= Activator.PLUGIN_ID;

	protected SVNClientManager cm;

	protected URLManager urlManager;

	protected File svnWorkingCopyDirectory;

	protected SVNManager(URLManager urlManager, String svnWorkingCopyDirectory, String username, String password) {
		this.urlManager= urlManager;
		this.svnWorkingCopyDirectory= new File(svnWorkingCopyDirectory);
		setupLibrary();
		cm= SVNClientManager.newInstance(null, username, password);
	}

	public static RemoteSVNManager createRemoteSVNManager(URLManager urlManager, String svnWorkingCopyDirectory, String username, String password) {
		return new RemoteSVNManager(urlManager, svnWorkingCopyDirectory, username, password);
	}

	public static LocalSVNManager createLocalSVNManager(String svnWorkingCopyDirectory) {
		return new LocalSVNManager(null, svnWorkingCopyDirectory, null, null);
	}

	//
	// Helper methods
	//
	public boolean isWorkingDirectoryValid() {
		try {
			cm.getWCClient().doInfo(svnWorkingCopyDirectory, SVNRevision.WORKING);
		} catch (SVNException e) {
			return false;
		}
		return true;
	}

	protected String extractUUIDFromFullPath(SVNURL fullPath) {
		String path= fullPath.getPath();
		int lastIndexOf= path.lastIndexOf('/');
		return path.substring(lastIndexOf + 1);
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
