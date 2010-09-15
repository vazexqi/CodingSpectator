package edu.illinois.refactorbehavior.svnkit.spike;

import static org.junit.Assert.assertNotSame;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNCommitClient;

/**
 * This is a simple class for demonstrating how to do an initial import and a delete of files from a
 * SVN repository (via HTTPS)
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class SVNImportTest {

	static final String URL= "https://subversion.assembla.com/svn/ganje/Experiment";

	static final String USERNAME= "nchen";

	static final String PASSWORD= "nchen";

	static final String FILE_NAME= "SVNImportTest.java";

	static final String MAC_ADDRESS= "c4:2c:03:0a:bc:24";

	private SVNCommitClient commitClient;

	@Before
	public void setup() throws SVNException {
		setupLibrary();
		SVNClientManager cm= SVNClientManager.newInstance(null, USERNAME, PASSWORD);
		commitClient= cm.getCommitClient();
	}

	@Test
	public void testInitialImport() throws SVNException {
		SVNURL url= SVNURL.parseURIEncoded(URL + '/' + USERNAME + '/' + MAC_ADDRESS + '/' + FILE_NAME);
		File file= new File("src/edu/illinois/refactorbehavior/svnkit/spike/" + FILE_NAME);
		SVNCommitInfo commitInfo= commitClient.doImport(file, url, "Initial import", null, false, false, SVNDepth.INFINITY);
		assertNotSame("The file was not added to the repository", SVNCommitInfo.NULL, commitInfo);
	}

	@After
	public void tearDown() throws SVNException {
		SVNURL url= SVNURL.parseURIEncoded(URL + '/' + USERNAME + '/' + MAC_ADDRESS + '/' + FILE_NAME);
		SVNCommitInfo deleteInfo= commitClient.doDelete(new SVNURL[] { url }, "Deleted test import");
		assertNotSame("The file was not removed from the repository", SVNCommitInfo.NULL, deleteInfo);
	}

	/*
	 * Initializes the library to work with a repository via different
	 * protocols.
	 */
	private static void setupLibrary() {
		/*
		 * For using over http:// and https://
		 */
		DAVRepositoryFactory.setup();
		/*
		 * For using over svn:// and svn+xxx://
		 */
		SVNRepositoryFactoryImpl.setup();

		/*
		 * For using over file:///
		 */
		FSRepositoryFactory.setup();
	}
}
