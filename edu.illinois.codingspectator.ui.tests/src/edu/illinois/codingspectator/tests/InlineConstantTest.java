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
public class InlineConstantTest extends CodingSpectatorTest {

	private static final String INLINE_CONSTANT_DIALOG_NAME= "Inline Constant";

	private static final String INLINE_CONSTANT_MENU_ITEM= "Inline...";

	static final String TEST_FILE_NAME= "InlineConstantTestFile";

	static final String PROJECT_NAME= "MyFirstProject_" + InlineConstantTest.class;

	void cancelRefactoring() {
		bot.shell(INLINE_CONSTANT_DIALOG_NAME).activate();
		bot.button(CANCEL_BUTTON_NAME).click();
	}

	void performRefactoring() {
		bot.shell(INLINE_CONSTANT_DIALOG_NAME).activate();
		bot.button(OK_BUTTON_NAME).click();
	}

	@Override
	public void prepareRefactoring() {
		SWTBotEclipseEditor editor= bot.editorByTitle(TEST_FILE_NAME + ".java").toTextEditor();

		// Inline Constant Refactoring
		editor.setFocus();
		editor.selectRange(4, 24, 32 - 24);

		SWTBotMenu refactorMenu= bot.menu(REFACTOR_MENU_NAME);
		assertTrue(refactorMenu.isEnabled());

		SWTBotMenu extractConstantMenuItem= refactorMenu.menu(INLINE_CONSTANT_MENU_ITEM);
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
