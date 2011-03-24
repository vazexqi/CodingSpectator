/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.rename;

import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * @author Mohsen Vakilian
 * @author nchen
 */
public class T21 extends RefactoringTest {

	@Override
	protected String getTestFileName() {
		return "RenameSourceFolderUnitTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectFromPackageExplorer(getProjectName(), "src");
		bot.invokeRefactoringFromMenu("Rename...");
		bot.fillTextField("New name:", "renamed-src");
		bot.clickButtons(IDialogConstants.OK_LABEL);
	}

}
