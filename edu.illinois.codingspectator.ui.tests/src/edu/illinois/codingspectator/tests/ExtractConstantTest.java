/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
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
public class ExtractConstantTest extends CodingSpectatorTest {

	private static final String EXTRACT_CONSTANT_MENU_ITEM= "Extract Constant...";

	private static final String EXTRACT_CONSTANT_DIALOG_NAME= "Extract Constant";

	static final String TEST_FILE_NAME= "ExtractConstantTestFile";

	static final String PROJECT_NAME= "MyFirstProject_" + ExtractConstantTest.class;

	@Override
	void cancelRefactoring() {
		bot.shell(EXTRACT_CONSTANT_DIALOG_NAME).activate();
		bot.button(CANCEL_BUTTON_NAME).click();
	}

	@Override
	void performRefactoring() {
		bot.shell(EXTRACT_CONSTANT_DIALOG_NAME).activate();
		bot.button(OK_BUTTON_NAME).click();
	}

	@Override
	public void prepareRefactoring() {
		SWTBotEclipseEditor editor= bot.editorByTitle(TEST_FILE_NAME + ".java").toTextEditor();

		// Extract Constant Refactoring
		editor.setFocus();
		editor.selectRange(5, 27, 34 - 27);

		SWTBotMenu refactorMenu= bot.menu(REFACTOR_MENU_NAME);
		assertTrue(refactorMenu.isEnabled());

		SWTBotMenu extractConstantMenuItem= refactorMenu.menu(EXTRACT_CONSTANT_MENU_ITEM);
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
