/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.introduceparameterobject;

import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * 
 * This test performs an introduce parameter object refactoring on a piece of code that will have
 * compilation problems before and after the refactoring.
 * 
 * @author Mohsen Vakilian
 * 
 */
public class T04 extends RefactoringTest {

	private static final String MENU_ITEM= "Introduce Parameter Object...";

	private static final String SELECTION= "m1";

	@Override
	protected String getTestFileName() {
		return "InvalidIntroduceParameterObjectTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 19, 9, SELECTION.length());
		bot.invokeRefactoringFromMenu(MENU_ITEM);
		bot.clickButtons(IDialogConstants.YES_LABEL, IDialogConstants.OK_LABEL);
	}

}
