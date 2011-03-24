package edu.illinois.codingspectator.ui.tests.extractmethod;

import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.RefactoringTest;

public class T05 extends RefactoringTest {

	protected static final String EXTRACT_METHOD_MENU_ITEM_NAME= "Extract Method...";

	private static final String SELECTION= "main";

	@Override
	protected String getTestFileName() {
		return "ValidExtractMethodTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 7, 23, SELECTION.length());
		bot.invokeRefactoringFromMenu(EXTRACT_METHOD_MENU_ITEM_NAME);
		bot.clickButtons(IDialogConstants.OK_LABEL);
	}

}
