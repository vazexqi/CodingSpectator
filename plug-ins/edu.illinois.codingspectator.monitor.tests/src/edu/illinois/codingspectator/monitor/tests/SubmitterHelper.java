/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.monitor.tests;

import java.io.OutputStream;
import java.io.PrintWriter;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNCommitClient;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCClient;

import edu.illinois.codingspectator.efs.EFSFile;
import edu.illinois.codingspectator.monitor.core.submission.SVNManager;
import edu.illinois.codingspectator.monitor.core.submission.URLManager;
import edu.illinois.codingspectator.monitor.ui.prefs.UUIDGenerator;
import edu.illinois.codingspectator.monitor.ui.submission.Submitter;

public class SubmitterHelper {

	static Submitter submitter;

	static final String USERNAME= "test.codingspectator";

	static final String PASSWORD= "test.codingspectator";

	static SVNWCClient workingCopyClient;

	static SVNCommitClient commitClient;

	static final String UUID= "00000000-0000-0000-0000-000000000000";

	static URLManager urlManager;

	static final String FILENAME= "log.txt";

	static SVNManager svnManager;

	static void initializeSubmitter() {
		submitter= new Submitter(new MockAuthenticationProvider(USERNAME, PASSWORD));
		urlManager= new URLManager(Messages.MockAuthenticationProvider_TestRepositoryURL, USERNAME, UUID);
		svnManager= new SVNManager(urlManager, Submitter.WATCHED_DIRECTORY, USERNAME, PASSWORD);
		SVNClientManager clientManager= SVNClientManager.newInstance(null, USERNAME, PASSWORD);
		workingCopyClient= clientManager.getWCClient();
		commitClient= clientManager.getCommitClient();
	}

	static void modifyFileInWatchedFolder() throws CoreException {
		PrintWriter printWriter= null;
		try {
			EFSFile logFile= new EFSFile(Submitter.WATCHED_DIRECTORY).append(FILENAME);
			OutputStream outputStream= logFile.getFileStore().openOutputStream(EFS.ATTRIBUTE_GROUP_READ | EFS.ATTRIBUTE_GROUP_WRITE, new NullProgressMonitor());
			printWriter= new PrintWriter(outputStream);
			printWriter.write(UUIDGenerator.generateID());
			printWriter.flush();
		} finally {
			printWriter.close();
		}
	}

	static long getFileRevisionNumber() throws SVNException {
		SVNURL url= urlManager.getSVNURL(urlManager.joinByURLSeparator(urlManager.getPersonalRepositoryURL(), FILENAME));
		SVNInfo info= workingCopyClient.doInfo(url, SVNRevision.HEAD, SVNRevision.HEAD);
		return info.getRevision().getNumber();
	}

}
