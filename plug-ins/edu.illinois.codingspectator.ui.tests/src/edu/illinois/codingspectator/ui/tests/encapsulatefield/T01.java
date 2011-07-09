/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.encapsulatefield;

import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * 
 * This test checks the log of a performed encapsulate field refactoring that is invoked by a
 * textual selection.
 * 
 * @author Mohsen Vakilian
 * 
 */
public class T01 extends RefactoringTest {

	private static final String MENU_ITEM= "Encapsulate Field...";

	private static final String SELECTION= "field";

	@Override
	protected String getTestFileName() {
		return "EncapsulateFieldTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 7, 11, SELECTION.length());
		bot.invokeRefactoringFromMenu(MENU_ITEM);
		bot.clickButtons(IDialogConstants.OK_LABEL);
	}

}
