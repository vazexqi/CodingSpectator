/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.pullup;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.RefactoringLog;
import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class CancelledValidMultiStepPullUpClass extends RefactoringTest {

	private static final String PULL_UP_MENU_ITEM= "Pull Up...";

	RefactoringLog refactoringLog= new RefactoringLog(RefactoringLog.LogType.CANCELLED);

	@Override
	protected String getTestFileName() {
		return "ValidPullUpFieldTestFile";
	}

	@Override
	protected String getTestInputLocation() {
		return "pull-up";
	}

	@Override
	protected void doRefactoringLogShouldBeEmpty() {
		assertFalse(refactoringLog.exists());
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileName(), 9, 6, "Child".length());
		bot.invokeRefactoringFromMenu(PULL_UP_MENU_ITEM);
		bot.clickButtons("Select All");
		bot.clickButtons(IDialogConstants.NEXT_LABEL, IDialogConstants.CANCEL_LABEL);
	}

	@Override
	protected void doRefactoringShouldBeLogged() {
		assertTrue(refactoringLog.exists());
	}

	@Override
	protected void doCleanRefactoringHistory() throws CoreException {
		refactoringLog.clean();
	}

}
