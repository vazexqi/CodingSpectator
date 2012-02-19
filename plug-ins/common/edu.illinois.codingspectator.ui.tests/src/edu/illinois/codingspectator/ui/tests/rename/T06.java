/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.rename;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.junit.Ignore;

import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * @author Balaji Ambresh Rajkumar
 * @author Mohsen Vakilian
 * @author nchen
 */
@Ignore("See issue #184.")
public class T06 extends RefactoringTest {

	@Override
	protected String getTestFileName() {
		return "RenamePackageTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectFromPackageExplorer(getProjectName());
		bot.invokeRefactoringFromMenu("Rename...");
		bot.fillTextField("New name:", "RenamedJavaProject");
		bot.clickButtons(IDialogConstants.CANCEL_LABEL);
	}

}
