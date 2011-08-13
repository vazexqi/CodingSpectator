/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.move;

import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.CodingSpectatorBot;
import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * This tests exercises the MoveImportDeclarationsPolicy processor. It performs a refactoring to
 * move multiple import statements from one source file to another.
 * 
 * @author Balaji Ambresh Rajkumar
 * @author Mohsen Vakilian
 * 
 */
public class T26 extends RefactoringTest {

	@Override
	protected String getTestFileName() {
		return "MoveImportsTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		final String destinationForImports= "DestinationFile";
		bot.createANewJavaClass(getProjectName(), destinationForImports);
		bot.selectFromPackageExplorer(getProjectName(), "src", CodingSpectatorBot.PACKAGE_NAME, getTestFileFullName());
		bot.getBot().menu("Navigate").menu("Open").click();
		final String selection= "import java.util.ArrayList;\n" +
										"import java.util.Queue;";
		bot.selectElementToRefactor(getTestFileFullName(), 5, 0, selection.length());
		bot.invokeRefactoringFromMenu("Move...");
		bot.activateShellWithName("Textual Move");
		bot.getCurrentTree().expandNode(getProjectName(), "src", CodingSpectatorBot.PACKAGE_NAME, getTestFileFullName());
		bot.getCurrentTree().pressShortcut(org.eclipse.jface.bindings.keys.KeyStroke.getInstance('D'));
		bot.clickButtons(IDialogConstants.OK_LABEL);
	}

}
