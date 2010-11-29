/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.monitor.core.submission;

import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNRevision;

/**
 * Local operations for SVNManager
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class LocalSVNManager extends SVNManager {

	protected LocalSVNManager(URLManager urlManager, String svnWorkingCopyDirectory, String username, String password) {
		super(urlManager, svnWorkingCopyDirectory, username, password);
	}

	public SVNInfo doInfo() throws SVNException {
		return cm.getWCClient().doInfo(svnWorkingCopyDirectory, SVNRevision.WORKING);
	}

	public String getSVNWorkingCopyUsername() {
		try {
			SVNInfo info= doInfo();
			return info.getAuthor();
		} catch (SVNException e) {
			// Do not log. This is a harmless operation. If nothing is available, we just default to ""
			return "";
		}
	}

	public String getSVNWorkingCopyRepositoryUUID() {
		try {
			SVNInfo info= doInfo();
			SVNURL fullPath= info.getURL();
			return extractUUIDFromFullPath(fullPath);
		} catch (SVNException e) {
			// Do not log. This is a harmless operation. If nothing is available, we just default to ""
			return "";
		}
	}

	public void doCleanup() throws SVNException {
		cm.getWCClient().doCleanup(svnWorkingCopyDirectory);
	}

	public void doAdd() throws SVNException {
		cm.getWCClient().doAdd(svnWorkingCopyDirectory, true, false, false, SVNDepth.INFINITY, false, false);
	}
}
