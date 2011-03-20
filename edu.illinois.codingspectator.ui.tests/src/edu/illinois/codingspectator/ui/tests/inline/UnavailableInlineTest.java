/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.inline;

import static org.junit.Assert.assertFalse;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.CapturedRefactoringDescriptor;
import edu.illinois.codingspectator.ui.tests.DescriptorComparator;
import edu.illinois.codingspectator.ui.tests.RefactoringLog;
import edu.illinois.codingspectator.ui.tests.RefactoringLogUtils;
import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * @author Mohsen Vakilian
 * @author nchen
 */
public class UnavailableInlineTest extends RefactoringTest {

	private static final String INLINE_CONSTANT_MENU_ITEM= "Inline...";

	private static final String SELECTION= "InlineConstantTestFile";

	RefactoringLog refactoringLog= new RefactoringLog(RefactoringLog.LogType.UNAVAILABLE);

	@Override
	protected String getTestFileName() {
		return "InlineConstantTestFile";
	}

	@Override
	protected String getTestInputLocation() {
		return "inline";
	}

	@Override
	protected void doRefactoringLogShouldBeEmpty() {
		assertFalse(refactoringLog.exists());
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 5, 13, SELECTION.length());
		bot.invokeRefactoringFromMenu(INLINE_CONSTANT_MENU_ITEM);
		bot.clickButtons(IDialogConstants.OK_LABEL);
	}

	@Override
	protected void doRefactoringShouldBeLogged() {
		CapturedRefactoringDescriptor capturedDescriptor= RefactoringLogUtils.getTheSingleRefactoringDescriptor(refactoringLog, getProjectName());
		CapturedRefactoringDescriptor expectedRefactoringDescriptor= RefactoringLogUtils.getTheSingleExpectedRefactoringDescriptor(getTestInputLocation() + "/" + getClass().getSimpleName(),
					getProjectName());
		DescriptorComparator.assertMatches(expectedRefactoringDescriptor, capturedDescriptor);
	}

	@Override
	protected void doCleanRefactoringHistory() throws CoreException {
		refactoringLog.clean();
	}

}
