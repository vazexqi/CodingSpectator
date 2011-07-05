/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.pushdown;

import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * @author Balaji Ambresh Rajkumar
 * 
 */
public class T01 extends RefactoringTest {

	private static final String PUSH_DOWN_MENU_ITEM= "Push Down...";

	private static final String SELECTION= "Child2";

	@Override
	protected String getTestFileName() {
		return "PushDownSingleFieldTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 14, 6, SELECTION.length());
		bot.invokeRefactoringFromMenu(PUSH_DOWN_MENU_ITEM);
		bot.clickButtons(IDialogConstants.OK_LABEL);
	}

}
