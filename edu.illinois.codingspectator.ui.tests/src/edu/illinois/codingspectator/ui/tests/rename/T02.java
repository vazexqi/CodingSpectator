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
public class T02 extends RefactoringTest {

	@Override
	protected String getTestFileName() {
		return "RenameInterfaceTypeTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 5, 10, "IUniversityEmployee".length());
		bot.invokeRefactoringFromMenu("Rename...");
		/* The second invocation of the rename refactoring brings up the dialog. */
		bot.invokeRefactoringFromMenu("Rename...");
		bot.fillTextField("New name:", "renamed_" + "IUniversityEmployee");
		bot.clickButtons(IDialogConstants.CANCEL_LABEL);
	}

}
