package edu.illinois.codingspectator.ui.tests.extractinterface;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.RefactoringTest;

public class T01 extends RefactoringTest {

	protected static final String EXTRACT_INTERFACE_ITEM_NAME= "Extract Interface...";

	private static final String SELECTION= "m1";

	private static final String SELECTED_CLASS= "Parent";

	private static final String NEW_INTERFACE_NAME= "I" + SELECTED_CLASS;

	@Override
	protected String getTestFileName() {
		return "ExtractInterfaceTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 6, 17, SELECTION.length());
		bot.invokeRefactoringFromMenu(EXTRACT_INTERFACE_ITEM_NAME);

		bot.fillTextField("Interface name:", NEW_INTERFACE_NAME);
		bot.clickButtons(IDialogConstants.OK_LABEL);
	}

	@Override
	protected void doRefactoringShouldBeLogged() throws CoreException {
		super.doRefactoringShouldBeLogged();
		printMessage("The captured selection text is different from what the test has actullay selected.");
	}

}
