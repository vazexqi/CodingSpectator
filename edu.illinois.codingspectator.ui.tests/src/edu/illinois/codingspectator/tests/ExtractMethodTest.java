package edu.illinois.codingspectator.tests;

import static org.junit.Assert.assertTrue;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.junit.runner.RunWith;

/**
 * @author Mohsen Vakilian
 * @author nchen
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class ExtractMethodTest extends RefactoringWatcherTest {

	private static final String VALID_EXTRACTED_METHOD= "validExtractedMethod";

	private static final String REFACTOR_MENU_ITEM_NAME= "Extract Method...";

	private static final String EXTRACT_METHOD_DIALOG_NAME= "Extract Method";

	static final String TEST_FILE_NAME= "ExtractMethodTestFile";

	static final String PROJECT_NAME= "MyFirstProject_" + ExtractMethodTest.class;

	@Override
	void cancelRefactoring() {
		bot.shell(EXTRACT_METHOD_DIALOG_NAME).activate();
		bot.button(CANCEL_BUTTON_NAME).click();
	}

	@Override
	void performRefactoring() {
		bot.shell(EXTRACT_METHOD_DIALOG_NAME).activate();
		bot.textWithLabel("Method name:").setText(VALID_EXTRACTED_METHOD);
		bot.button(OK_BUTTON_NAME).click();
	}

	@Override
	public void prepareRefactoring() {
		SWTBotEclipseEditor editor= bot.editorByTitle(TEST_FILE_NAME + ".java").toTextEditor();

		editor.setFocus();
		editor.selectRange(7, 8, 38 - 8);

		SWTBotMenu refactorMenu= bot.menu(REFACTOR_MENU_NAME);
		assertTrue(refactorMenu.isEnabled());

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

}
