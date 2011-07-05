/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.rename;

import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * This test performs a rename refactoring on a plain folder.
 * 
 * @author Mohsen Vakilian
 * 
 */
public class T26 extends RefactoringTest {

	@Override
	protected void doAddJavaClass() throws Exception {
		//This test doesn't need a Java class.
	}

	@Override
	protected String getTestFileName() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void doExecuteRefactoring() throws Exception {
		bot.selectJavaProject(getProjectName());
		bot.getBot().menu("File").menu("New").menu("Folder").click();
		bot.activateShellWithName("New Folder");
		String folderName= "folder";
		bot.fillTextField("Folder name:", folderName);
		bot.clickButtons(IDialogConstants.FINISH_LABEL);

		bot.selectFromPackageExplorer(getProjectName(), folderName);
		bot.invokeRefactoringFromMenu("Rename...");
		bot.fillTextField("New name:", "Renamed" + folderName);
		bot.clickButtons(IDialogConstants.OK_LABEL);
	}

}
