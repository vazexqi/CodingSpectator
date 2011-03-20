package edu.illinois.codingspectator.ui.tests.extractinterface;

import static org.junit.Assert.assertFalse;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.CapturedRefactoringDescriptor;
import edu.illinois.codingspectator.ui.tests.DescriptorComparator;
import edu.illinois.codingspectator.ui.tests.RefactoringLog;
import edu.illinois.codingspectator.ui.tests.RefactoringLogUtils;
import edu.illinois.codingspectator.ui.tests.RefactoringTest;

public class ValidPerformedExtractInterfaceParentMethodTest extends RefactoringTest {

	protected static final String EXTRACT_INTERFACE_ITEM_NAME= "Extract Interface...";

	private static final String SELECTION= "m1";

	private static final String SELECTED_CLASS= "Parent";

	private static final String NEW_INTERFACE_NAME= "I" + SELECTED_CLASS;

	RefactoringLog performedRefactoringLog= new RefactoringLog(RefactoringLog.LogType.PERFORMED);

	RefactoringLog eclipseRefactoringLog= new RefactoringLog(RefactoringLog.LogType.ECLIPSE);

	@Override
	protected String getTestFileName() {
		return "ExtractInterfaceTestFile";
	}

	@Override
	protected String getTestInputLocation() {
		return "extract-interface";
	}

	@Override
	protected void doRefactoringLogShouldBeEmpty() {
		assertFalse(performedRefactoringLog.exists());
		assertFalse(eclipseRefactoringLog.exists());
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 6, 17, SELECTION.length());
		bot.invokeRefactoringFromMenu(EXTRACT_INTERFACE_ITEM_NAME);

		bot.fillTextField("Interface name:", NEW_INTERFACE_NAME);
		bot.clickButtons(IDialogConstants.OK_LABEL);
	}

	@Override
	protected void doRefactoringShouldBeLogged() {
		System.err.println("The captured selection text is different from what the test has actullay selected.");
		performedLogShouldBeCorrect();
		eclipseLogShouldBeCorrect();
	}

	private void performedLogShouldBeCorrect() {
		CapturedRefactoringDescriptor capturedDescriptor= RefactoringLogUtils.getTheSingleRefactoringDescriptor(performedRefactoringLog, getProjectName());
		CapturedRefactoringDescriptor expectedRefactoringDescriptor= RefactoringLogUtils.getTheSingleExpectedRefactoringDescriptor(getTestInputLocation() + "/" + getClass().getSimpleName()
				+ "/performed", getProjectName());
		DescriptorComparator.assertMatches(expectedRefactoringDescriptor, capturedDescriptor);
	}

	private void eclipseLogShouldBeCorrect() {
		CapturedRefactoringDescriptor capturedDescriptor= RefactoringLogUtils.getTheSingleRefactoringDescriptor(eclipseRefactoringLog, getProjectName());
		CapturedRefactoringDescriptor expectedRefactoringDescriptor= RefactoringLogUtils.getTheSingleExpectedRefactoringDescriptor(getTestInputLocation() + "/" + getClass().getSimpleName()
				+ "/eclipse", getProjectName());
		DescriptorComparator.assertMatches(expectedRefactoringDescriptor, capturedDescriptor);
	}

	@Override
	protected void doCleanRefactoringHistory() throws CoreException {
		performedRefactoringLog.clean();
		eclipseRefactoringLog.clean();
	}

}
