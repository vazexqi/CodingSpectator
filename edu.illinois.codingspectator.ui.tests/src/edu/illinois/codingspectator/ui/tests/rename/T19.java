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
public class T19 extends RefactoringTest {

	@Override
	protected String getTestFileName() {
		return "RenamePackageTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectFromPackageExplorer(getProjectName(), "src", "edu.illinois.codingspectator");
		bot.invokeRefactoringFromMenu("Rename...");
		bot.fillTextField("New name:", "renamed.edu.illinois.codingspectator");
		bot.clickButtons(IDialogConstants.OK_LABEL);
	}

}
