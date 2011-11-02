/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.monitor.tests;

import java.io.OutputStream;
import java.io.PrintWriter;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.p2.core.UIServices.AuthenticationInfo;
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

public class MockSubmitterFactory {

	static final String UUID= "00000000-0000-0000-0000-000000000000";

	static final String FILENAME= "log.txt";

	private String username;

	private String password;

	private Submitter submitter;

	private SVNWCClient workingCopyClient;

	private SVNCommitClient commitClient;

	private URLManager urlManager;

	private SVNManager svnManager;

	public MockSubmitterFactory(MockParticipant participant) {
		username= participant.getUsername();
		password= participant.getPassword();
		submitter= new Submitter(new MockAuthenticationProvider(getAuthenticationInfo()));
		urlManager= new URLManager(Messages.MockAuthenticationProvider_TestRepositoryURL, username, UUID);
		svnManager= new SVNManager(urlManager, Submitter.WATCHED_FOLDER, username, password);
		SVNClientManager clientManager= SVNClientManager.newInstance(null, username, password);
		workingCopyClient= clientManager.getWCClient();
		commitClient= clientManager.getCommitClient();
	}

	public MockSubmitterFactory() {
		this(MockParticipantFactory.getMockParticipant(0));
	}

	public Submitter getSubmitter() {
		return submitter;
	}

	public SVNWCClient getWorkingCopyClient() {
		return workingCopyClient;
	}

	public SVNCommitClient getCommitClient() {
		return commitClient;
	}

	public URLManager getURLManager() {
		return urlManager;
	}

	public SVNManager getSVNManager() {
		return svnManager;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public void modifyFileInWatchedFolder() throws CoreException {
		PrintWriter printWriter= null;
		try {
			EFSFile logFile= new EFSFile(Submitter.WATCHED_FOLDER).append(FILENAME);
			OutputStream outputStream= logFile.getFileStore().openOutputStream(EFS.ATTRIBUTE_GROUP_READ | EFS.ATTRIBUTE_GROUP_WRITE, new NullProgressMonitor());
			printWriter= new PrintWriter(outputStream);
			printWriter.write(UUIDGenerator.generateID());
			printWriter.flush();
		} finally {
			printWriter.close();
		}
	}

	public long getFileRevisionNumber() throws SVNException {
		SVNURL url= urlManager.getSVNURL(urlManager.joinByURLSeparator(urlManager.getPersonalWorkspaceURL(), FILENAME));
		SVNInfo info= workingCopyClient.doInfo(url, SVNRevision.HEAD, SVNRevision.HEAD);
		return info.getRevision().getNumber();
	}

	public AuthenticationInfo getAuthenticationInfo() {
		return new AuthenticationInfo(username, password, false);
	}

}
