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
public class RenameNonVirtualMethod extends CodingSpectatorTest {

	private static final String RENAME_NONVIRTUAL_METHOD_DIALOG_NAME= "Rename Method";

	private static final String RENAME_NONVIRTUAL_METHOD_MENU_ITEM= "Rename...";

	static final String TEST_FILE_NAME= "RenameNonVirtualMethodTestFile";

	@Override
	protected String getRefactoringDialogName() {
		return RENAME_NONVIRTUAL_METHOD_DIALOG_NAME;
	}

	@Override
	public void selectElementToRefactor() {
		selectElementToRefactor(7, 16, 32 - 16);
	}

	@Override
	String getTestFileName() {
		return TEST_FILE_NAME;
	}
	
	@Override
	protected void configureRefactoring() {
		super.configureRefactoring();
		
		final String originalVariableName = bot.textWithLabel("New name:").getText();
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
	protected String refactoringMenuItemName() {
		return RENAME_NONVIRTUAL_METHOD_MENU_ITEM;
	}

}
