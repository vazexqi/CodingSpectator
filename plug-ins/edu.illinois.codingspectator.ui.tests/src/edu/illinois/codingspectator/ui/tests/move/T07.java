/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.move;

import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.CodingSpectatorBot;
import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * @author Balaji Ambresh Rajkumar
 */
public class T07 extends RefactoringTest {

	@Override
	protected String getTestFileName() {
		return "MoveCusTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectFromPackageExplorer(getProjectName(), "src", CodingSpectatorBot.PACKAGE_NAME, getTestFileFullName());
		bot.invokeRefactoringFromMenu("Move...");
		bot.activateShellWithName("Move");
		bot.getCurrentTree().pressShortcut(org.eclipse.jface.bindings.keys.KeyStroke.getInstance('.'));

		bot.clickButtons(IDialogConstants.OK_LABEL);
	}

}
