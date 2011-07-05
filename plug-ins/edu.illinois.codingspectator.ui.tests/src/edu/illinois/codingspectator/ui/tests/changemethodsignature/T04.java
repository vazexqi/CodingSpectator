/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.changemethodsignature;

import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * 
 * This test tries to invoke the change method signature refactoring on an element where this
 * refactoring is not possible.
 * 
 * @author Mohsen Vakilian
 * 
 */
public class T04 extends RefactoringTest {

	private static final String MENU_ITEM= "Change Method Signature...";

	private static final String SELECTION= "C";

	@Override
	protected String getTestFileName() {
		return "ChangeMethodSignatureTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 5, 6, SELECTION.length());
		bot.invokeRefactoringFromMenu(MENU_ITEM);
		bot.clickButtons(IDialogConstants.OK_LABEL);
	}

}
