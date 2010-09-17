package edu.illinois.refactoringwatcher.monitor.tests;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.illinois.refactoringwatcher.monitor.prefs.PrefsFacade;
import edu.illinois.refactoringwatcher.monitor.submission.Submitter;
import edu.illinois.refactoringwatcher.monitor.submission.Submitter.InitializationException;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class TestImport {

	static Submitter submitter;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		PrefsFacade.setNetid("nchen");
		PrefsFacade.setUUID("00000000-0000-0000-0000-000000000000");
		submitter= new Submitter("nchen", "nchen");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void shouldImport() throws InitializationException {
		submitter.initialize();
		// Check that the working directory has been created locally.
		assertTrue("svn import failed to create a local working directory.", new File(Submitter.watchedDirectory + File.separator + ".svn").exists());
		// Check that the ltk directory has been created in the repository.
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

}
