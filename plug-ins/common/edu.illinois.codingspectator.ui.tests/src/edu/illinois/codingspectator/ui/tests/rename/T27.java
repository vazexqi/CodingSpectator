/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.rename;

import edu.illinois.codingspectator.ui.tests.CodingSpectatorBot;
import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * 
 * This test quits the refactoring by closing the dialog.
 * 
 * @author Mohsen Vakilian
 * 
 */
public class T27 extends RefactoringTest {

	@Override
	protected String getTestFileName() {
		return "RenameCompilationUnitTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectFromPackageExplorer(getProjectName(), "src", CodingSpectatorBot.PACKAGE_NAME, getTestFileFullName());
		bot.invokeRefactoringFromMenu("Rename...");
		bot.fillTextField("New name:", "Renamed" + getTestFileName());
		bot.activateShellWithName("Rename Compilation Unit").close();
	}

}
