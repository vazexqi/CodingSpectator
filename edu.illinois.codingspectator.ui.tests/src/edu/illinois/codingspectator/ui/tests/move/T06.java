/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.move;

import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * @author Mohsen Vakilian
 * @author nchen
 */
public class T06 extends RefactoringTest {

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
		String destinationType= "edu.illinois.codingspectator.C2";
		bot.setComboBox(destinationTypeLabel, destinationType);
		bot.clickButtons(IDialogConstants.OK_LABEL);
	}

}
