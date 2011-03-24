/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.pushdown;

import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * @author Balaji Ambresh Rajkumar
 */
public class T07 extends RefactoringTest {

	private static final String PUSH_DOWN_MENU_ITEM= "Push Down...";

	@Override
	protected String getTestFileName() {
		return "ValidPushDownMultiFieldNotInSubclassesTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		final String selection= "private int field1ToBePushedDown;\n    public int field2ToBePushedDown;";
		bot.selectElementToRefactor(getTestFileFullName(), 6, 4, selection.length());
		bot.invokeRefactoringFromMenu(PUSH_DOWN_MENU_ITEM);
		bot.clickButtons("Select All", IDialogConstants.OK_LABEL);
	}

}
