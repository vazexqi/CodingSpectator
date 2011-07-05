/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.move;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogConstants;
import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * This tests exercises the MovePackageFragmentRootsPolicy processor. It performs a refactoring to
 * move the source folder from one project to a folder in another project.
 * 
 * @author Mohsen Vakilian
 * @author Balaji Ambresh Rajkumar
 */

public class T18 extends RefactoringTest {
	
	@Override
	protected String getTestFileName() {
		return "MoveCusTestFile";
	}
	
	@Override
	protected void doExecuteRefactoring() throws CoreException {
		final String secondProjectName= getProjectName() + '2';
		bot.createANewJavaProject(secondProjectName);
		bot.selectFromPackageExplorer(getProjectName(), "src");
		bot.invokeRefactoringFromMenu("Move...");

		bot.activateShellWithName("Move");
		bot.getCurrentTree().expandNode(secondProjectName);
		bot.getCurrentTree().pressShortcut(org.eclipse.jface.bindings.keys.KeyStroke.getInstance('.'));
		bot.clickButtons(IDialogConstants.OK_LABEL);

		bot.deleteProject(secondProjectName);
	}
}
