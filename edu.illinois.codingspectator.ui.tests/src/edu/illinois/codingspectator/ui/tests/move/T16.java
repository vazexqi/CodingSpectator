/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.move;

import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.CodingSpectatorBot;
import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * @author Mohsen Vakilian
 * @author nchen
 */
public class T16 extends RefactoringTest {

	@Override
	protected String getTestFileName() {
		return "MoveCusTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectFromPackageExplorer(getProjectName(), "src", CodingSpectatorBot.PACKAGE_NAME, getTestFileFullName());
		bot.invokeRefactoringFromMenu("Move...");
		bot.clickButtons("Create Package...");
		bot.fillTextField("Name:", "edu.illinois.codingspectator.subpackage");
		bot.clickButtons(IDialogConstants.FINISH_LABEL, IDialogConstants.CANCEL_LABEL);
	}

}
