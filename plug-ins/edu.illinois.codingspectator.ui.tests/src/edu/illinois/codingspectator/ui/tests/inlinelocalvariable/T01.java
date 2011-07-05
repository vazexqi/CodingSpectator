/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.inlinelocalvariable;

import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * 
 * If a final field is not initialized, the compiler will report an error. If the user tries to
 * inline an uninitialized local variable, an dialog will show up saying that the refactoring is not
 * allowed on an the selected variable. This test checks that the user's attempt to invoke a
 * refactoring on an uninitialized local variable gets captured properly.
 * 
 * @author Mohsen Vakilian
 * @author nchen
 */
public class T01 extends RefactoringTest {

	private static final String MENU_ITEM= "Inline...";

	private static final String SELECTION= "localVariable";

	@Override
	protected String getTestFileName() {
		return "UninitializedLocalVariableTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 8, 15, SELECTION.length());
		bot.invokeRefactoringFromMenu(MENU_ITEM);
		bot.clickButtons(IDialogConstants.OK_LABEL);
	}

}
