/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.usesupertype;

import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * @author Balaji Ambresh Rajkumar
 * @author Mohsen Vakilian
 * @author nchen
 */
public class T01 extends RefactoringTest {

	@Override
	protected String getTestFileName() {
		return "UseSuperTypeTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 3, 0, "package".length());
		bot.invokeRefactoringFromMenu("Use Supertype Where Possible...");
		bot.clickButtons(IDialogConstants.OK_LABEL);
	}

}
