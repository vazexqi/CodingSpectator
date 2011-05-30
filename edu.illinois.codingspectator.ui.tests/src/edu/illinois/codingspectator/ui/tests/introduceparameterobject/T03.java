/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.introduceparameterobject;

import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * 
 * This test invokes an introduce parameter object refactoring on a piece of code where the
 * refactoring is not applicable.
 * 
 * @author Mohsen Vakilian
 * 
 */
public class T03 extends RefactoringTest {

	private static final String MENU_ITEM= "Introduce Parameter Object...";

	private static final String SELECTION= "D";

	@Override
	protected String getTestFileName() {
		return "IntroduceParameterObjectTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 16, 6, SELECTION.length());
		bot.invokeRefactoringFromMenu(MENU_ITEM);
		bot.clickButtons(IDialogConstants.OK_LABEL);
	}

}
