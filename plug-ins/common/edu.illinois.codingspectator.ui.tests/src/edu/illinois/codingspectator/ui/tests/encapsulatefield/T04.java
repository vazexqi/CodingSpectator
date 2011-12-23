/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.encapsulatefield;

import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * 
 * This test checks the log of an attempt to invoke the encapsulate field refactoring on a field
 * with an unresolved type.
 * 
 * @author Mohsen Vakilian
 * 
 */
public class T04 extends RefactoringTest {

	private static final String MENU_ITEM= "Encapsulate Field...";

	private static final String SELECTION= "m";

	@Override
	protected String getTestFileName() {
		return "EncapsulateUnresolvedFieldTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 7, 6, SELECTION.length());
		bot.invokeRefactoringFromMenu(MENU_ITEM);
		bot.clickButtons(IDialogConstants.OK_LABEL);
	}

}
