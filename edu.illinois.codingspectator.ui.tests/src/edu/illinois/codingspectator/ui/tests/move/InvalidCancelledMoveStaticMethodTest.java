/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.move;

import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.RefactoringLog;
import edu.illinois.codingspectator.ui.tests.RefactoringLog.LogType;
import edu.illinois.codingspectator.ui.tests.RefactoringLogChecker;
import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * @author Mohsen Vakilian
 * @author nchen
 */
public class InvalidCancelledMoveStaticMethodTest extends RefactoringTest {

	RefactoringLog refactoringLog= new RefactoringLog(RefactoringLog.LogType.CANCELLED);

	@Override
	protected String getTestFileName() {
		return "MoveStaticMemberTestFile";
	}

	@Override
	protected String getTestInputLocation() {
		return "move";
	}

	@Override
	protected void doRefactoringLogShouldBeEmpty() {
		assertFalse(refactoringLog.exists());
	}

	@Override
	protected void doExecuteRefactoring() {
		String selectedMember= "m()";
		bot.selectElementToRefactor(getTestFileFullName(), 9, 16, selectedMember.length());
		bot.invokeRefactoringFromMenu("Move...");
		String destinationTypeLabel= String.format("Destination type for '%s':", selectedMember);
		String destinationType= "edu.illinois.codingspectator.C3";
		bot.setComboBox(destinationTypeLabel, destinationType);
		bot.clickButtons(IDialogConstants.CANCEL_LABEL);
	}

	@Override
	protected Collection<RefactoringLogChecker> getRefactoringLogCheckers() {
		return Arrays.asList(new RefactoringLogChecker(LogType.CANCELLED, getTestInputLocation(), getClass().getSimpleName(), getProjectName()));
	}

	@Override
	protected void doCleanRefactoringHistory() throws CoreException {
		refactoringLog.clean();
	}

}
