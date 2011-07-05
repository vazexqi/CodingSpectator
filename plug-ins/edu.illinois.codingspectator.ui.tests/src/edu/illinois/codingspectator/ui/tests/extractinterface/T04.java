/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.extractinterface;

import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.RefactoringTest;

public class T04 extends RefactoringTest {

	protected static final String EXTRACT_INTERFACE_ITEM_NAME= "Extract Interface...";

	private final static String SELECTION= "";

	@Override
	protected String getTestFileName() {
		return "ExtractInterfaceTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 17, 1, SELECTION.length());
		bot.invokeRefactoringFromMenu(EXTRACT_INTERFACE_ITEM_NAME);
		bot.clickButtons(IDialogConstants.OK_LABEL);
	}

}
