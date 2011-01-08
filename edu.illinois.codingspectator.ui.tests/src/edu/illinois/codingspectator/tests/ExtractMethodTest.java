/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.tests;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
abstract public class ExtractMethodTest extends CodingSpectatorTest {

	protected static final String METHOD_NAME_LABEL= "Method name:";

	protected static final String EXTRACT_METHOD_MENU_ITEM_NAME= "Extract Method...";

	protected static final String EXTRACT_METHOD_DIALOG_NAME= "Extract Method";

	abstract protected String getExtractedMethodName();

	@Override
	protected String getRefactoringDialogName() {
		return EXTRACT_METHOD_DIALOG_NAME;
	}

	@Override
	protected String refactoringMenuItemName() {
		return EXTRACT_METHOD_MENU_ITEM_NAME;
	}

	@Override
	protected void performRefactoring() {
		bot.shell(getRefactoringDialogName()).activate();
		bot.textWithLabel(METHOD_NAME_LABEL).setText(getExtractedMethodName());
		bot.button(OK_BUTTON_NAME).click();
	}

}
