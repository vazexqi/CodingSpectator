/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.tests;

import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.runner.RunWith;

/**
 * @author Balaji Ambresh Rajkumar
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class RenameVirtualMethodTest extends CodingSpectatorTest {

	private static final String RENAME_VIRTUAL_METHOD_DIALOG_NAME= "Rename Method";

	private static final String RENAME_VIRTUAL_METHOD_MENU_ITEM= "Rename...";

	static final String TEST_FILE_NAME= "RenameVirtualMethodTestFile";

	@Override
	protected String getRefactoringDialogName() {
		return RENAME_VIRTUAL_METHOD_DIALOG_NAME;
	}

	@Override
	public void selectElementToRefactor() {
		selectElementToRefactor(15, 16, 29 - 16);
	}

	@Override
	String getTestFileName() {
		return TEST_FILE_NAME;
	}

	@Override
	protected void configureRefactoringToPerform() {
		super.configureRefactoringToPerform();
		configureRefactoring();
	};

	@Override
	protected void configureRefactoringToCancel() {
		super.configureRefactoringToCancel();
		configureRefactoring();
	}

	protected void configureRefactoring() {
		final String originalVariableName= bot.textWithLabel("New name:").getText();
		bot.textWithLabel("New name:").setText("renamed_" + originalVariableName);
	}

	/**
	 * Invoking the Rename menu option twice from the Refactor menu brings up the Rename dialog.
	 * Since we're focussing on renaming the overridden function at the derived class, a warning
	 * dialog pops up. Dismiss this dialg before moving to the actual refactoring.
	 */
	@Override
	protected void invokeRefactoring() {
		forceDerivedClassMethodRename();
		dismissWarningDialog();
	}

	/**
	 * Dismiss the warning dialog that comes up when we try renaming the method from the derived
	 * class.
	 */
	private void dismissWarningDialog() {
		bot.shell("Rename Refactoring").activate();
		bot.button(YES_BUTTON_LABEL).click();
	}

	/**
	 * Invoking the Rename menu option twice from the Refactor menu brings up the Rename warning
	 * dialog.
	 */
	private void forceDerivedClassMethodRename() {
		super.invokeRefactoring();
		super.invokeRefactoring();

	}

	@Override
	protected String refactoringMenuItemName() {
		return RENAME_VIRTUAL_METHOD_MENU_ITEM;
	}

}
