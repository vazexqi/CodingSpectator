package edu.illinois.refactorbehavior;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.FileUtils;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;


/**
 * @author Mohsen Vakilian
 * @author nchen
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class ExtractConstantTest extends RefactoringWatcherTest {

	static final String TEST_FILE_NAME= "ExtractConstantTestFile";

	static final String PROJECT_NAME= "MyFirstProject_" + ExtractConstantTest.class;

	@Test
	public void canSetupProject() throws Exception {
		super.canCreateANewJavaProject();
		super.canCreateANewJavaClass();
	}

	@Test
	public void canTypeInTextInAJavaClass() throws Exception {

		Bundle bundle= Platform
				.getBundle("edu.illinois.refactorbehavior.tests");
		String contents= FileUtils.read(bundle.getEntry("test-files/"
				+ TEST_FILE_NAME + ".java"));

		SWTBotEclipseEditor editor= bot.editorByTitle(TEST_FILE_NAME + ".java")
				.toTextEditor();
		editor.setText(contents);
		editor.save();
	}

	///////////////////////////////////////////////////////////////////////////
	//
	// SWTBot tests run in order which is why we can take advantage of this
	// and capture the canceled refactoring first and then do the actual 
	// refactoring. Currently we are just testing that the appropriates folders 
	// are created.
	//
	///////////////////////////////////////////////////////////////////////////

	@Test
	public void currentRefactoringsCapturedShouldBeEmpty() {
		assertFalse(performedRefactorings.exists());
		assertFalse(canceledRefactorings.exists());
	}

	@Test
	public void shouldCaptureCancelledRefactoring() {
		prepareRefactoring();

		bot.shell("Extract Constant").activate();
		bot.button("Cancel").click();
	}

	@Test
	// This needs to be interleaved here after the refactoring has been canceled.
	public void currentRefactoringsCanceledShouldBePopulated() {
		assertFalse(performedRefactorings.exists());
		assertTrue(canceledRefactorings.exists());
	}

	@Test
	public void shouldCapturePerformedRefactoring() throws Exception {
		prepareRefactoring();

		bot.shell("Extract Constant").activate();
		bot.button("OK").click();

	}

	@Test
	// This needs to be interleaved here after the refactoring has been performed.
	public void currentRefactoringsPerformedShouldBePopulated() {
		assertTrue(performedRefactorings.exists());
		assertTrue(canceledRefactorings.exists());
	}

	@Override
	protected void prepareRefactoring() {
		SWTBotEclipseEditor editor= bot.editorByTitle(TEST_FILE_NAME + ".java").toTextEditor();

		// Extract Constant Refactoring
		editor.setFocus();
		editor.selectRange(5, 27, 34 - 27);

		SWTBotMenu refactorMenu= bot.menu("Refactor");
		assertTrue(refactorMenu.isEnabled());
		
		SWTBotMenu extractConstantMenuItem= refactorMenu.menu("Extract Constant...");
		assertTrue(extractConstantMenuItem.isEnabled());
		
		extractConstantMenuItem.click();
	}

	@Override
	String getProjectName() {
		return PROJECT_NAME;
	}

	@Override
	String getTestFileName() {
		return TEST_FILE_NAME;
	}



}
