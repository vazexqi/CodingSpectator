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
public class T04 extends RefactoringTest {

	private static final String INLINE_CONSTANT_MENU_ITEM= "Inline...";

	private static final String SELECTION= "InlineConstantTestFile";

	@Override
	protected String getTestFileName() {
		return "InlineConstantTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 5, 13, SELECTION.length());
		bot.invokeRefactoringFromMenu(INLINE_CONSTANT_MENU_ITEM);
		bot.clickButtons(IDialogConstants.OK_LABEL);
	}

}
