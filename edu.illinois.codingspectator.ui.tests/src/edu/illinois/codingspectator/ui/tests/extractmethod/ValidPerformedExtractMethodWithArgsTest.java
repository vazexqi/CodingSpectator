package edu.illinois.codingspectator.ui.tests.extractmethod;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.RefactoringLog;
import edu.illinois.codingspectator.ui.tests.RefactoringLog.LogType;
import edu.illinois.codingspectator.ui.tests.RefactoringLogChecker;
import edu.illinois.codingspectator.ui.tests.RefactoringTest;

public class ValidPerformedExtractMethodWithArgsTest extends RefactoringTest {

	protected static final String EXTRACT_METHOD_MENU_ITEM_NAME= "Extract Method...";

	private static final String SELECTION= "System.out.println(args);";

	private static final String METHOD_NAME= "extractedMethod";

	RefactoringLog performedRefactoringLog= new RefactoringLog(RefactoringLog.LogType.PERFORMED);

	RefactoringLog eclipseRefactoringLog= new RefactoringLog(RefactoringLog.LogType.ECLIPSE);

	@Override
	protected String getTestFileName() {
		return "ValidExtractMethodTestFile";
	}

	@Override
	protected String getTestInputLocation() {
		return "extract-method";
	}

	@Override
	protected Collection<RefactoringLogChecker> getRefactoringLogCheckers() {
		return Arrays.asList(new RefactoringLogChecker(LogType.PERFORMED, getTestInputLocation(), getClass().getSimpleName(), getProjectName()), new RefactoringLogChecker(LogType.ECLIPSE,
				getTestInputLocation(), getClass().getSimpleName(), getProjectName()));
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 9, 8, SELECTION.length());
		bot.invokeRefactoringFromMenu(EXTRACT_METHOD_MENU_ITEM_NAME);

		bot.fillTextField("Method name:", METHOD_NAME);
		bot.clickButtons(IDialogConstants.OK_LABEL);
	}

}
