/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.pushdown;

import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * @author Balaji Ambresh Rajkumar
 */
public class T09 extends RefactoringTest {

	private static final String PUSH_DOWN_MENU_ITEM= "Push Down...";

	@Override
	protected String getTestFileName() {
		return "PushDownMethodTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 6, 17, "m1".length());
		bot.invokeRefactoringFromMenu(PUSH_DOWN_MENU_ITEM);
		bot.clickButtons(IDialogConstants.OK_LABEL);
	}

}
