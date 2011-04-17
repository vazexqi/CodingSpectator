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
public class T16 extends RefactoringTest {

	// To keep state across different @Test methods, we need a static field
	private static String renamedProjectName;

	@Override
	protected String getTestFileName() {
		return "RenamePackageTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectFromPackageExplorer(getProjectName());
		bot.invokeRefactoringFromMenu("Rename...");
		renamedProjectName= "RenamedJavaProject";
		bot.fillTextField("New name:", renamedProjectName);
		bot.clickButtons(IDialogConstants.OK_LABEL);
	}

	@Override
	public String getProjectName() {
		if (renamedProjectName == null) {
			return super.getProjectName();
		}
		return renamedProjectName;
	}

}
