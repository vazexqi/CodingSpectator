/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.move;

import org.eclipse.jface.dialogs.IDialogConstants;
import edu.illinois.codingspectator.ui.tests.CodingSpectatorBot;
import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * This tests exercises the MoveSubCuElementsPolicy processor. It performs a refactoring to move one
 * class inside another class
 * 
 * @author Balaji Ambresh Rajkumar
 */
public class T23 extends RefactoringTest {

	@Override
	protected String getTestFileName() {
		return "MoveCuTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		final String selectedMember= "E";
		bot.selectElementToRefactor(getTestFileFullName(), 13, 6, selectedMember.length());
		bot.invokeRefactoringFromMenu("Move...");
		bot.activateShellWithName("Textual Move");
		bot.getCurrentTree().expandNode(getProjectName(), "src", CodingSpectatorBot.PACKAGE_NAME, getTestFileFullName());
		bot.getCurrentTree().pressShortcut(org.eclipse.jface.bindings.keys.KeyStroke.getInstance('D'));
		bot.clickButtons(IDialogConstants.OK_LABEL);
	}
}
