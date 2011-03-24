/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.move;

import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * @author Mohsen Vakilian
 * @author nchen
 */
public class T02 extends RefactoringTest {

	@Override
	protected String getTestFileName() {
		return "MoveInstanceMethodTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 11, 9, "m3".length());
		bot.invokeRefactoringFromMenu("Move...");
		bot.clickButtons(IDialogConstants.CANCEL_LABEL);
	}

}
