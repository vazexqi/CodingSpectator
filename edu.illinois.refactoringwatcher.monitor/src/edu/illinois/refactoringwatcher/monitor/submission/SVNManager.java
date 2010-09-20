package edu.illinois.refactoringwatcher.monitor.submission;

import java.io.File;

import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;

import edu.illinois.refactoringwatcher.monitor.Activator;

/**
 * This is the concrete implementation of the {@link Submitter} design contract for an SVN backend.
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class SVNManager {

	private static final String COMMIT_MESSAGE= Activator.PLUGIN_ID;

	private String repositoryBaseURL;

	private SVNClientManager cm;

	public SVNManager(String repositoryLocation, String username, String password) {
		this.repositoryBaseURL= repositoryLocation;

		setupLibrary();
		cm= SVNClientManager.newInstance(null, username, password);
	}

	/**
	 * @idempotent.
	 */
	public void doImport(String directory, String repositoryOffsetURL) throws SVNException {
		if (isWorkingDirectoryValid(directory))
			return;
		File file= new File(directory);
		cm.getCommitClient().doImport(file, getAbsoluteURL(repositoryOffsetURL), "Initial import", null, false, true, SVNDepth.INFINITY);
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

	/**
	 * @idempotent.
	 */
	public void doCheckout(String destinationPath, String repositoryOffsetURL) throws SVNException {
		File destinationPathFile= new File(destinationPath);
		cm.getUpdateClient().doCheckout(getAbsoluteURL(repositoryOffsetURL), destinationPathFile, SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.INFINITY,
				true);
	}

	private SVNURL getAbsoluteURL(String repositoryOffsetURL) throws SVNException {
		return SVNURL.parseURIEncoded(repositoryBaseURL + "/" + repositoryOffsetURL);
	}

	public void doAdd(String pathToAdd) throws SVNException {
		File pathToAddFile= new File(pathToAdd);
		cm.getWCClient().doAdd(pathToAddFile, true, false, false, SVNDepth.INFINITY, false, false);
	}

	public void doCommit(String pathToCommit) throws SVNException {
		File[] pathToCommitFiles= new File[] { new File(pathToCommit) };
		cm.getCommitClient().doCommit(pathToCommitFiles, false, COMMIT_MESSAGE, null, null, false, true, SVNDepth.INFINITY);
	}

}
