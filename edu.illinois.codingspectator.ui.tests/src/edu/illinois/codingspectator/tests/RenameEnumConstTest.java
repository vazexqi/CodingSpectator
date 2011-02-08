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
public class RenameEnumConstTest extends CodingSpectatorTest {

	private static final String RENAME_ENUM_CONSTANT_DIALOG_NAME= "Rename Enum Constant";

	private static final String RENAME_ENUM_CONSTANT_MENU_ITEM= "Rename...";

	static final String TEST_FILE_NAME= "RenameEnumConstantTestFile";

	@Override
	protected String getRefactoringDialogName() {
		return RENAME_ENUM_CONSTANT_DIALOG_NAME;
	}

	@Override
	public void selectElementToRefactor() {
		selectElementToRefactor(6, 8, 11 - 8);
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
	 */
	@Override
	protected void invokeRefactoring() {
		super.invokeRefactoring();
		super.invokeRefactoring();
	}

	@Override
	protected String[] getRefactoringDialogPerformButtonSequence() {
		return new String[] { OK_BUTTON_LABEL, CONTINUE_BUTTON_LABEL };
	}

	@Override
	protected String refactoringMenuItemName() {
		return RENAME_ENUM_CONSTANT_MENU_ITEM;
	}

}
