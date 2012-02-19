/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.move;

import org.eclipse.jface.dialogs.IDialogConstants;
import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * This tests exercises the MovePackageFragmentRootsPolicy processor. It performs a refactoring to
 * move the source folder to another folder within the same project.
 * 
 * @author Mohsen Vakilian
 * @author Balaji Ambresh Rajkumar
 */
public class T17 extends RefactoringTest {

	@Override
	protected String getTestFileName() {
		return "MoveCusTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectFromPackageExplorer(getProjectName(), "src");
		bot.invokeRefactoringFromMenu("Move...");

		bot.activateShellWithName("Move");
		bot.getCurrentTree().expandNode(getProjectName());
		bot.getCurrentTree().pressShortcut(org.eclipse.jface.bindings.keys.KeyStroke.getInstance('.'));
		bot.clickButtons(IDialogConstants.OK_LABEL);
	}
}
