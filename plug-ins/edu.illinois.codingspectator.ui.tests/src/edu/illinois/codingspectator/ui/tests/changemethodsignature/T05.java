/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.changemethodsignature;

import edu.illinois.codingspectator.ui.tests.CodingSpectatorBot;
import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * 
 * This tests quits the refactoring by closing the dialog.
 * 
 * @author Mohsen Vakilian
 * 
 */
public class T05 extends RefactoringTest {

	private static final String MENU_ITEM= "Change Method Signature...";

	@Override
	protected String getTestFileName() {
		return "ChangeMethodSignatureTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectFromPackageExplorer(getProjectName(), "src", CodingSpectatorBot.PACKAGE_NAME, getTestFileFullName(), "C", "m1(Object) : void");
		bot.invokeRefactoringFromMenu(MENU_ITEM);
		bot.clickButtons("Add");
		bot.activateShellWithName("Change Method Signature").close();
	}

}
