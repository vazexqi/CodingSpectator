/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.move;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.junit.Ignore;

import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * @author Mohsen Vakilian
 * @author nchen
 */
@Ignore("See issue #181.")
public class T15 extends RefactoringTest {

	@Override
	protected String getTestFileName() {
		return "MoveInstanceMethodTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 7, 9, "m2".length());
		bot.invokeRefactoringFromMenu("Move...");
		bot.clickButtons(IDialogConstants.OK_LABEL);
	}

}
