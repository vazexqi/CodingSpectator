/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.changemethodsignature;

import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.CodingSpectatorBot;
import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * 
 * This test performs a change method signature refactoring that results in compilation problems.
 * This refactoring reports an error to the user because it tries to remove a parameter that is in
 * use.
 * 
 * @author Mohsen Vakilian
 * 
 */
public class T03 extends RefactoringTest {

	private static final String MENU_ITEM= "Change Method Signature...";

	private static final String SELECTION= "m1";

	@Override
	protected String getTestFileName() {
		return "ChangeMethodSignatureTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 7, 9, SELECTION.length());
		bot.invokeRefactoringFromMenu(MENU_ITEM);
		bot.clickButtons("Remove", IDialogConstants.OK_LABEL);
		bot.clickButtons(CodingSpectatorBot.CONTINUE_LABEL);
	}

}
