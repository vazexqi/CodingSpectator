package edu.illinois.codingspectator.ui.tests.extractinterface;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.RefactoringLog.LogType;
import edu.illinois.codingspectator.ui.tests.RefactoringLogChecker;
import edu.illinois.codingspectator.ui.tests.RefactoringTest;

public class UnavailableExtractInterfaceTest extends RefactoringTest {

	protected static final String EXTRACT_INTERFACE_ITEM_NAME= "Extract Interface...";

	private final static String SELECTION= "";

	@Override
	protected String getTestFileName() {
		return "ExtractInterfaceTestFile";
	}

	@Override
	protected String getTestInputLocation() {
		return "extract-interface";
	}

	@Override
	protected Collection<RefactoringLogChecker> getRefactoringLogCheckers() {
		return Arrays.asList(new RefactoringLogChecker(LogType.UNAVAILABLE, getTestInputLocation(), getClass().getSimpleName(), getProjectName()));
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 17, 1, SELECTION.length());
		bot.invokeRefactoringFromMenu(EXTRACT_INTERFACE_ITEM_NAME);
		bot.clickButtons(IDialogConstants.OK_LABEL);
	}

}
