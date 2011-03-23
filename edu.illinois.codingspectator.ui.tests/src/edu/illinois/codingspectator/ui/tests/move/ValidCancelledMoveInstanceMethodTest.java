/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.move;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.RefactoringLog.LogType;
import edu.illinois.codingspectator.ui.tests.RefactoringLogChecker;
import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * @author Mohsen Vakilian
 * @author nchen
 */
public class ValidCancelledMoveInstanceMethodTest extends RefactoringTest {

	@Override
	protected String getTestFileName() {
		return "MoveInstanceMethodTestFile";
	}

	@Override
	protected String getTestInputLocation() {
		return "move";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 7, 9, "m2".length());
		bot.invokeRefactoringFromMenu("Move...");
		bot.clickButtons(IDialogConstants.CANCEL_LABEL);
	}

	@Override
	protected Collection<RefactoringLogChecker> getRefactoringLogCheckers() {
		return Arrays.asList(new RefactoringLogChecker(LogType.CANCELLED, getTestInputLocation(), getClass().getSimpleName(), getProjectName()));
	}

}
