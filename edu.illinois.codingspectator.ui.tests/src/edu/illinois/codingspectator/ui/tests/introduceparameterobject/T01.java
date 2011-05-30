/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.introduceparameterobject;

import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.CodingSpectatorBot;
import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * 
 * This test performs an introduce parameter object refactoring.
 * 
 * @author Mohsen Vakilian
 * 
 */
public class T01 extends RefactoringTest {

	private static final String MENU_ITEM= "Introduce Parameter Object...";

	@Override
	protected String getTestFileName() {
		return "IntroduceParameterObjectTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectFromPackageExplorer(getProjectName(), "src", CodingSpectatorBot.PACKAGE_NAME, getTestFileFullName(), "C", "m1(Object, Object) : void");
		bot.invokeRefactoringFromMenu(MENU_ITEM);
		bot.clickButtons(IDialogConstants.OK_LABEL);
	}

}
