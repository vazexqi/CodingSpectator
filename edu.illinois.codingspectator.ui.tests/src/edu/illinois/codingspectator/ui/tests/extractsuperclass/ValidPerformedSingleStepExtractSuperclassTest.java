/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.extractsuperclass;

import static org.junit.Assert.assertFalse;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.CapturedRefactoringDescriptor;
import edu.illinois.codingspectator.ui.tests.DescriptorComparator;
import edu.illinois.codingspectator.ui.tests.RefactoringLog;
import edu.illinois.codingspectator.ui.tests.RefactoringLogUtils;
import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class ValidPerformedSingleStepExtractSuperclassTest extends RefactoringTest {

	private static final String EXTRACT_SUPERCLASS_MENU_ITEM= "Extract Superclass...";

	protected static final String SUPERCLASS_NAME_LABEL= "Superclass name:";

	private final static String SELECTION= "methodToBePulledUp";

	private final static String NEW_SUPERCLASS_NAME= "NewSuperClassName";

	RefactoringLog performedRefactoringLog= new RefactoringLog(RefactoringLog.LogType.PERFORMED);

	RefactoringLog eclipseRefactoringLog= new RefactoringLog(RefactoringLog.LogType.ECLIPSE);

	@Override
	protected String getTestFileName() {
		return "ExtractSuperclassTestFile";
	}

	@Override
	protected String getTestInputLocation() {
		return "extract-superclass";
	}

	@Override
	protected void doRefactoringLogShouldBeEmpty() {
		assertFalse(performedRefactoringLog.exists());
		assertFalse(eclipseRefactoringLog.exists());
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 11, 16, SELECTION.length());
		bot.invokeRefactoringFromMenu(EXTRACT_SUPERCLASS_MENU_ITEM);
		bot.fillTextField(SUPERCLASS_NAME_LABEL, NEW_SUPERCLASS_NAME);
		bot.clickButtons(IDialogConstants.FINISH_LABEL);
	}

	@Override
	protected void doRefactoringShouldBeLogged() {
		performedLogShouldBeCorrect();
		eclipseLogShouldBeCorrect();
	}

	private void performedLogShouldBeCorrect() {
		System.err.println("CodingSpectator may not capture the destination type for extract super class correctly.");
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
