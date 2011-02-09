/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.inlineconstant;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.RefactoringLog;
import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * @author Mohsen Vakilian
 * @author nchen
 */
public class PerformedInlineConstantTest extends RefactoringTest {

	private static final String INLINE_CONSTANT_MENU_ITEM= "Inline...";

	RefactoringLog refactoringLog= new RefactoringLog(RefactoringLog.LogType.PERFORMED);

	@Override
	protected String getTestFileName() {
		return "InlineConstantTestFile";
	}

	@Override
	protected String getTestInputLocation() {
		return "inline-constant";
	}

	@Override
	protected void doRefactoringLogShouldBeEmpty() {
		assertFalse(refactoringLog.exists());
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileName(), 7, 24, "CONSTANT".length());
		bot.invokeRefactoringFromMenu(INLINE_CONSTANT_MENU_ITEM);
		bot.clickButtons(IDialogConstants.OK_LABEL);
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
