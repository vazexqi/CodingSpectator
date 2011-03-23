/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.move;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.CodingSpectatorBot;
import edu.illinois.codingspectator.ui.tests.RefactoringLog.LogType;
import edu.illinois.codingspectator.ui.tests.RefactoringLogChecker;
import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * @author Mohsen Vakilian
 * @author nchen
 */
public class T04 extends RefactoringTest {

	@Override
	protected String getTestFileName() {
		return "MoveStaticMemberTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		String selectedMember= "m()";
		bot.selectElementToRefactor(getTestFileFullName(), 9, 16, selectedMember.length());
		bot.invokeRefactoringFromMenu("Move...");
		String destinationTypeLabel= String.format("Destination type for '%s':", selectedMember);
		String destinationType= "edu.illinois.codingspectator.C3";
		bot.setComboBox(destinationTypeLabel, destinationType);
		bot.clickButtons(IDialogConstants.OK_LABEL, CodingSpectatorBot.CONTINUE_LABEL);
	}

	@Override
	protected Collection<RefactoringLogChecker> getRefactoringLogCheckers() {
		return Arrays.asList(new RefactoringLogChecker(LogType.PERFORMED, getRefactoringKind(), getClass().getSimpleName(), getProjectName()), new RefactoringLogChecker(LogType.ECLIPSE,
				getRefactoringKind(), getClass().getSimpleName(), getProjectName()));
	}
}
