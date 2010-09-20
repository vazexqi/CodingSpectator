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
import edu.illinois.refactoringwatcher.monitor.submission.Submitter.SubmissionException;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class TestSubmitter {

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
	public void shouldInitialize() throws InitializationException {
		submitter.initialize();
		// Check that the working directory has been created locally.
		assertTrue("Failed to initialize the submitter.", new File(Submitter.watchedDirectory + File.separator + ".svn").exists());
		// TODO: Check that the ltk directory has been created in the repository.
	}

	@Test
	public void shouldSubmit() throws SubmissionException, InitializationException {
		submitter.initialize();
		submitter.submit();
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

}
