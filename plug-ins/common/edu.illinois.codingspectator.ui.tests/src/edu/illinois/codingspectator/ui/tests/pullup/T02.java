/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.pullup;

import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class T02 extends RefactoringTest {

	private static final String PULL_UP_MENU_ITEM= "Pull Up...";


	@Override
	protected String getTestFileName() {
		return "InvalidPullUpMethodTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 14, 9, "m".length());
		bot.invokeRefactoringFromMenu(PULL_UP_MENU_ITEM);
		bot.clickButtons(IDialogConstants.FINISH_LABEL, IDialogConstants.FINISH_LABEL);
	}

}
