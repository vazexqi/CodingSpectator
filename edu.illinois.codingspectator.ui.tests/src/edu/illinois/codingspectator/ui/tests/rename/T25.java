/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.rename;

import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * @author Mohsen Vakilian
 * @author nchen
 */
public class T25 extends RefactoringTest {

	@Override
	protected String getTestFileName() {
		return "RenameAcrossMultipleFilesTestFile1";
	}

	@Override
	protected void doExecuteRefactoring() throws Exception {
		final String otherFile= "RenameAcrossMultipleFilesTestFile2";
		bot.createANewJavaClass(getProjectName(), otherFile);
		bot.prepareJavaTextInEditor(getRefactoringKind(), "RenameAcrossMultipleFilesTestFile2.java");

		final String selection= "CONST";
		bot.selectElementToRefactor("RenameAcrossMultipleFilesTestFile2.java", 10, 50, selection.length());
		bot.invokeRefactoringFromMenu("Rename...");
		bot.invokeRefactoringFromMenu("Rename...");
		bot.fillTextField("New name:", "CONSTRENAMED");
		bot.clickButtons(IDialogConstants.OK_LABEL);
	}
}
