package edu.illinois.refactorbehavior;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.FileUtils;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;

@RunWith(SWTBotJunit4ClassRunner.class)
public class ExtractConstantTest {

	static final String REFACTORING_HISTORY_LOCATION= Platform.getStateLocation(Platform.getBundle("org.eclipse.ltk.core.refactoring")).toOSString();

	static final String CANCELED_REFACTORINGS= ".refactorings.canceled";

	static final String PERFORMED_REFACTORINGS= ".refactorings.performed";

	static final String PACKAGE_NAME= "edu.illinois.refactorbehavior";

	static final String TEST_NAME= "TestFile";

	static final String PROJECT_NAME= "MyFirstProject_" + ExtractConstantTest.class;

	static SWTWorkbenchBot bot;

	private File performedRefactorings;

	private File canceledRefactorings;

	@BeforeClass
	public static void beforeClass() throws Exception {
		bot= new SWTWorkbenchBot();
		bot.viewByTitle("Welcome").close();
	}

	public ExtractConstantTest() {
		performedRefactorings= new File(REFACTORING_HISTORY_LOCATION + System.getProperty("file.separator") + PERFORMED_REFACTORINGS);
		canceledRefactorings= new File(REFACTORING_HISTORY_LOCATION + System.getProperty("file.separator") + CANCELED_REFACTORINGS);
	}

	@Test
	public void canCreateANewJavaProject() throws Exception {
		bot.menu("File").menu("New").menu("Project...").click();

		bot.shell("New Project").activate();
		bot.tree().expandNode("Java").select("Java Project");
		bot.button("Next >").click();

		bot.textWithLabel("Project name:").setText(PROJECT_NAME);

		bot.button("Finish").click();

		bot.button("Yes").click();
	}

	@Test
	public void canCreateANewJavaClass() throws Exception {
		bot.menu("File").menu("New").menu("Class").click();

		bot.shell("New Java Class").activate();
		bot.textWithLabel("Source folder:").setText(PROJECT_NAME + "/src");

		bot.textWithLabel("Package:").setText(PACKAGE_NAME);
		bot.textWithLabel("Name:").setText(TEST_NAME);

		bot.button("Finish").click();

	}

	@Test
	public void canTypeInTextInAJavaClass() throws Exception {

		Bundle bundle= Platform
				.getBundle("edu.illinois.refactorbehavior.tests");
		String contents= FileUtils.read(bundle.getEntry("test-files/"
				+ TEST_NAME + ".java"));

		SWTBotEclipseEditor editor= bot.editorByTitle(TEST_NAME + ".java")
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
	public void currentRefactoringsPerformedShouldBePopulated() {
		assertTrue(performedRefactorings.exists());
		assertTrue(canceledRefactorings.exists());
	}

	private void prepareRefactoring() {
		SWTBotEclipseEditor editor= bot.editorByTitle(TEST_NAME + ".java").toTextEditor();

		// Extract Constant Refactoring
		editor.setFocus();
		editor.selectRange(5, 27, 34 - 27);
		System.out.println(editor.getSelection());
		System.out.println(editor.cursorPosition());
		SWTBotMenu refactorMenu= bot.menu("Refactor");
		assertTrue(refactorMenu.isEnabled());
		SWTBotMenu extractConstantMenuItem= refactorMenu.menu("Extract Constant...");
		assertTrue(extractConstantMenuItem.isEnabled());
		extractConstantMenuItem.click();
	}


	@AfterClass
	public static void sleep() {
		bot.sleep(1000);
	}

}
