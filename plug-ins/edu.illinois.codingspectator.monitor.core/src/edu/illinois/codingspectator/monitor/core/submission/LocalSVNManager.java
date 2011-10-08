/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.monitor.core.submission;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNRevision;

import edu.illinois.codingspectator.efs.EFSFile;

/**
 * Local Subversion operations
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class LocalSVNManager extends AbstractSVNManager {

	protected final SVNClientManager cm;

	public LocalSVNManager(String svnWorkingCopyDirectory) {
		super(svnWorkingCopyDirectory);
		cm= SVNClientManager.newInstance(null, null, null);
	}

	public boolean isWorkingDirectoryValid() {
		try {
			cm.getWCClient().doInfo(svnWorkingCopyDirectory, SVNRevision.WORKING);
		} catch (SVNException e) {
			return false;
		}
		return true;
	}

	public SVNInfo doInfo() throws SVNException {
		return cm.getWCClient().doInfo(svnWorkingCopyDirectory, SVNRevision.WORKING);
	}

	public long getRevisionNumber() throws SVNException {
		return doInfo().getRevision().getNumber();
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

	private static String extractUUIDFromFullPath(SVNURL fullPath) {
		String path= fullPath.getPath();
		int lastIndexOf= path.lastIndexOf('/');
		return path.substring(lastIndexOf + 1);
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

	private void removeSVNMetaData(File path) throws CoreException {
		for (File child : path.listFiles()) {
			if (child.isDirectory()) {
				if (".svn".equals(child.getName())) {
					new EFSFile(child.getAbsolutePath()).delete();
				} else {
					removeSVNMetaData(child);
				}
			}
		}
	}

	public void removeSVNMetaData() throws CoreException {
		removeSVNMetaData(svnWorkingCopyDirectory);
	}

}
