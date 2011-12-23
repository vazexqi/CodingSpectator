/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.encapsulatefield;

import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.CodingSpectatorBot;
import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * 
 * This test invokes an encapsulate field refactoring by making a structural selection. Then, it
 * previews and performs the refactoring.
 * 
 * @author Mohsen Vakilian
 * 
 */
public class T06 extends RefactoringTest {

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
		bot.clickButtons(CodingSpectatorBot.PREVIEW_LABEL, IDialogConstants.OK_LABEL);
	}

}
