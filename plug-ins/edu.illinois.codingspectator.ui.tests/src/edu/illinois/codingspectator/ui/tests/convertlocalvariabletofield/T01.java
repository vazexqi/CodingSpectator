/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.convertlocalvariabletofield;

import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * 
 * This test performs a convert local variable to field refactoring.
 * 
 * @author Mohsen Vakilian
 * 
 */
public class T01 extends RefactoringTest {

	private static final String MENU_ITEM= "Convert Local Variable to Field...";

	private static final String SELECTION= "localVariable";

	@Override
	protected String getTestFileName() {
		return "ConvertLocalVariableToFieldTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 11, 27, SELECTION.length());
		bot.invokeRefactoringFromMenu(MENU_ITEM);
		bot.clickButtons(IDialogConstants.OK_LABEL);
	}

}
