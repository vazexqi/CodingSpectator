/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.inline;

import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * @author Mohsen Vakilian
 * @author nchen
 */
public class T03 extends RefactoringTest {

	private static final String INLINE_MENU_ITEM= "Inline...";

	private static final String SELECTION= "args";

	@Override
	protected String getTestFileName() {
		return "InlineConstantTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 9, 37, SELECTION.length());
		bot.invokeRefactoringFromMenu(INLINE_MENU_ITEM);
		bot.clickButtons(IDialogConstants.OK_LABEL);
	}

}
