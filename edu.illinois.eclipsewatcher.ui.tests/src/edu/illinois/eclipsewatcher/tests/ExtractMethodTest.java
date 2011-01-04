package edu.illinois.eclipsewatcher.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.illinois.eclipsewatcher.test.utils.FileUtilities;

/**
 * @author Mohsen Vakilian
 * @author nchen
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class ExtractMethodTest extends RefactoringWatcherTest {

	private static final String REFACTOR_MENU_ITEM_NAME= "Extract Method...";

	private static final String REFACTOR_MENU_NAME= "Refactor";

	private static final String DIALOG_NAME= "Extract Method";

	static final String TEST_FILE_NAME= "ExtractMethodTestFile";

	static final String PROJECT_NAME= "MyFirstProject_" + ExtractMethodTest.class;

	@Test
	public void canSetupProject() throws Exception {
		super.canCreateANewJavaProject();
		super.canCreateANewJavaClass();
		super.prepareJavaTextInEditor();
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

		bot.shell(DIALOG_NAME).activate();
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

		bot.shell(DIALOG_NAME).activate();
		bot.button("OK").click();
	}

	@Test
	// This needs to be interleaved here after the refactoring has been performed.
	public void currentRefactoringsPerformedShouldBePopulated() {
		bot.sleep(2000);
		assertTrue(canceledRefactorings.exists());
		assertTrue(performedRefactorings.exists());
	}

	// This is a hack to ensure that refactorings are cleared at the end of each test
	@Test
	public void cleanRefactoringHistory() {
		bot.sleep(2000);
		FileUtilities.cleanDirectory(performedRefactorings);
		FileUtilities.cleanDirectory(canceledRefactorings);
	}

	@Override
	protected void prepareRefactoring() {
		SWTBotEclipseEditor editor= bot.editorByTitle(TEST_FILE_NAME + ".java").toTextEditor();

		// Extract Constant Refactoring
		editor.setFocus();
		editor.selectRange(7, 8, 38 - 8);

		Display.getDefault().syncExec(new WaitForEvents());

		SWTBotMenu refactorMenu= bot.menu(REFACTOR_MENU_NAME);
		assertTrue(refactorMenu.isEnabled());

		Display.getDefault().syncExec(new WaitForEvents());

		SWTBotMenu extractConstantMenuItem= refactorMenu.menu(REFACTOR_MENU_ITEM_NAME);
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

	/*
	 * See http://help.eclipse.org/helios/index.jsp?topic=/org.eclipse.platform.doc.isv/guide/swt_threading.htm
	 */
	class WaitForEvents implements Runnable {

		@Override
		public void run() {
			while (Display.getDefault().readAndDispatch())
				;
		}

	}
}
