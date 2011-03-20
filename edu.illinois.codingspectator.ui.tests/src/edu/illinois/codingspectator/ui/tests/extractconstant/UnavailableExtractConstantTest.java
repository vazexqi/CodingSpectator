package edu.illinois.codingspectator.ui.tests.extractconstant;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.RefactoringLog.LogType;
import edu.illinois.codingspectator.ui.tests.RefactoringLogChecker;
import edu.illinois.codingspectator.ui.tests.RefactoringTest;

public class UnavailableExtractConstantTest extends RefactoringTest {

	private static final String EXTRACT_CONSTANT_MENU_ITEM= "Extract Constant...";

	private static final String TEST_FILE_NAME= "ExtractConstantTestFile";

	private final String SELECTION= "main";

	@Override
	protected String getTestFileName() {
		return TEST_FILE_NAME;
	}

	@Override
	protected String getTestInputLocation() {
		return "extract-constant";
	}

	@Override
	protected Collection<RefactoringLogChecker> getRefactoringLogCheckers() {
		return Arrays.asList(new RefactoringLogChecker(LogType.UNAVAILABLE, getTestInputLocation(), getClass().getSimpleName(), getProjectName()));
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 7, 23, SELECTION.length());
		bot.invokeRefactoringFromMenu(EXTRACT_CONSTANT_MENU_ITEM);
		bot.clickButtons(IDialogConstants.OK_LABEL);
	}

}
