/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.rename;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.RefactoringLog;
import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * @author Balaji Ambresh Rajkumar
 * @author Mohsen Vakilian
 * @author nchen
 */
public class ValidPerformedRenameLocalVariableTest extends RefactoringTest {

	RefactoringLog refactoringLog= new RefactoringLog(RefactoringLog.LogType.PERFORMED);

	@Override
	protected String getTestFileName() {
		return "RenameLocalVariableTestFile";
	}

	@Override
	protected String getTestInputLocation() {
		return "rename";
	}

	@Override
	protected void doRefactoringLogShouldBeEmpty() {
		assertFalse(refactoringLog.exists());
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 8, 12, "originalVariable".length());
		bot.invokeRefactoringFromMenu("Rename...");
		/* The second invocation of the rename refactoring brings up the dialog. */
		bot.invokeRefactoringFromMenu("Rename...");
		bot.fillTextField("New name:", "renamedOriginalVariable");
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
