/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.changemethodsignature;

import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * 
 * This test cancels a change method signature refactoring. This refactoring reports an error to the
 * user because it tries to remove a parameter that is in use.
 * 
 * @author Mohsen Vakilian
 * 
 */
public class T02 extends RefactoringTest {

	private static final String MENU_ITEM= "Change Method Signature...";

	private static final String SELECTION= "m1";

	@Override
	protected String getTestFileName() {
		return "ChangeMethodSignatureTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 7, 9, SELECTION.length());
		bot.invokeRefactoringFromMenu(MENU_ITEM);
		bot.clickButtons("Remove", IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL);
	}

	@Override
	protected void doLogsShouldBeCorrect() throws Exception {
		super.doLogsShouldBeCorrect();
		printMessage("This test throws an NPE. But, if you perform the steps of this test manually, you won't get any exceptions.");
	}

}
