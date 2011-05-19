/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.pushdown;

import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * @author Balaji Ambresh Rajkumar
 * @author Mohsen Vakilian
 */
public class T02 extends RefactoringTest {

	private static final String PUSH_DOWN_MENU_ITEM= "Push Down...";

	@Override
	protected String getTestFileName() {
		return "PushDownMethodTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 6, 17, "m1".length());
		bot.invokeRefactoringFromMenu(PUSH_DOWN_MENU_ITEM);
		bot.clickButtons("Deselect All", IDialogConstants.CANCEL_LABEL);
	}

	@Override
	protected void doLogsShouldBeCorrect() throws Exception {
		super.doLogsShouldBeCorrect();
		printMessage("This test does not select any members. Therefore, CodingSpectator cannot find an ITypeRoot. Thus, CodingSpectator fails to capture any selection information.");
	}

}
