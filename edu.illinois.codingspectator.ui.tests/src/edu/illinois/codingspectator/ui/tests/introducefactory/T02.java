/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.introducefactory;

import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.CodingSpectatorBot;
import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * 
 * This test cancels an introduce factory method.
 * 
 * @author Mohsen Vakilian
 * 
 */
public class T02 extends RefactoringTest {

	private static final String MENU_ITEM= "Introduce Factory...";

	@Override
	protected String getTestFileName() {
		return "IntroduceFactoryTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectFromPackageExplorer(getProjectName(), "src", CodingSpectatorBot.PACKAGE_NAME, getTestFileFullName(), getTestFileName(), "IntroduceFactoryTestFile(Object)");
		bot.invokeRefactoringFromMenu(MENU_ITEM);
		bot.clickButtons(IDialogConstants.CANCEL_LABEL);
	}

}
