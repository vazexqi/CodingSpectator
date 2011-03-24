package edu.illinois.codingspectator.ui.tests.extractconstant;

import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.RefactoringTest;

public class T02 extends RefactoringTest {

	private static final String EXTRACT_CONSTANT_MENU_ITEM= "Extract Constant...";

	private static final String TEST_FILE_NAME= "ExtractConstantTestFile";

	private final String SELECTION= "main";

	@Override
	protected String getTestFileName() {
		return TEST_FILE_NAME;
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 7, 23, SELECTION.length());
		bot.invokeRefactoringFromMenu(EXTRACT_CONSTANT_MENU_ITEM);
		bot.clickButtons(IDialogConstants.OK_LABEL);
	}

}
