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
public class T13 extends RefactoringTest {

	protected String getTestFileName() {
		return "RenameVirtualMethodTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 15, 16, "virtualMethod".length());
		bot.invokeRefactoringFromMenu("Rename...");
		/* The second invocation of the rename refactoring brings up the dialog. */
		bot.invokeRefactoringFromMenu("Rename...");
		// We need to dismiss a warning message that pops up when trying to rename a method from a subclass.
		bot.clickButtons(IDialogConstants.YES_LABEL);
		bot.fillTextField("New name:", "renamedVirtualMethod");
		bot.clickButtons(IDialogConstants.CANCEL_LABEL);
	}

}
