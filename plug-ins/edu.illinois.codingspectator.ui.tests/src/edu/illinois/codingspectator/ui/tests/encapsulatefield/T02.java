/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.encapsulatefield;

import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.CodingSpectatorBot;
import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * 
 * This test checks the log of a canceled encapsulate field refactoring that is invoked by a
 * structured selection.
 * 
 * @author Mohsen Vakilian
 * 
 */
public class T02 extends RefactoringTest {

	private static final String MENU_ITEM= "Encapsulate Field...";

	private static final String SELECTION= "field";

	@Override
	protected String getTestFileName() {
		return "EncapsulateFieldTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectFromPackageExplorer(getProjectName(), "src", CodingSpectatorBot.PACKAGE_NAME, getTestFileFullName(), getTestFileName(), SELECTION);
		bot.invokeRefactoringFromMenu(MENU_ITEM);
		bot.getBot().radio("keep field reference").click();
		bot.clickButtons(IDialogConstants.CANCEL_LABEL);
	}

}
