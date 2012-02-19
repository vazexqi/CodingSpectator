/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.extractconstant;

import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * @author Mohsen Vakilian
 * @author nchen
 */
public class T03 extends RefactoringTest {

	private static final String EXTRACT_CONSTANT_MENU_ITEM= "Extract Constant...";

	static final String TEST_FILE_NAME= "ExtractConstantTestFile";

	private static final String SELECTION= "\"Test0\"";

	@Override
	protected String getTestFileName() {
		return TEST_FILE_NAME;
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 8, 27, SELECTION.length());
		bot.invokeRefactoringFromMenu(EXTRACT_CONSTANT_MENU_ITEM);
		bot.clickButtons(IDialogConstants.CANCEL_LABEL);
	}

}
