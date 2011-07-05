/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.rename;

import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * @author Balaji Ambresh Rajkumar
 * @author Mohsen Vakilian
 * @author nchen
 */
public class T07 extends RefactoringTest {

	@Override
	protected String getTestFileName() {
		return "RenameLocalVariableTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 8, 12, "originalVariable".length());
		bot.invokeRefactoringFromMenu("Rename...");
		/* The second invocation of the rename refactoring brings up the dialog. */
		bot.invokeRefactoringFromMenu("Rename...");
		bot.fillTextField("New name:", "renamedOriginalVariable");
		bot.clickButtons(IDialogConstants.CANCEL_LABEL);
	}

}
