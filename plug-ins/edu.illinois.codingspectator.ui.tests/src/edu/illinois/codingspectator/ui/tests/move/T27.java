/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.move;

import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.CodingSpectatorBot;
import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * This tests exercises the MoveMembersPolicy processor. It cancels the move of multiple instance
 * variables from one class to another.
 * 
 * @author Balaji Ambresh Rajkumar
 * @author Mohsen Vakilian
 * 
 */
public class T27 extends RefactoringTest {

	@Override
	protected String getTestFileName() {
		return "MoveInstanceMembersTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		final String destinationForImports= "DestinationFile";
		bot.createANewJavaClass(getProjectName(), destinationForImports);
		bot.sleep();
		bot.selectFromPackageExplorer(getProjectName(), "src", CodingSpectatorBot.PACKAGE_NAME, getTestFileFullName());
		bot.getBot().menu("Navigate").menu("Open").click();
		final String selection= "    private int i;\n" +
				"    private int j;";
		bot.selectElementToRefactor(getTestFileFullName(), 7, 4, selection.length());
		bot.invokeRefactoringFromMenu("Move...");
		bot.activateShellWithName("Textual Move");
		bot.getCurrentTree().expandNode(getProjectName(), "src", CodingSpectatorBot.PACKAGE_NAME, getTestFileFullName());
		bot.getCurrentTree().pressShortcut(org.eclipse.jface.bindings.keys.KeyStroke.getInstance('D'));
		bot.clickButtons(IDialogConstants.CANCEL_LABEL);
	}

}
