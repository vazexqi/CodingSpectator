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
public class T05 extends RefactoringTest {

	@Override
	protected String getTestFileName() {
		return "UseSuperTypeTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 24, 6, "Child".length());
		bot.invokeRefactoringFromMenu("Use Supertype Where Possible...");

		bot.activateShellWithName("Use Super Type Where Possible");
		bot.getCurrentTree().pressShortcut(org.eclipse.jface.bindings.keys.KeyStroke.getInstance('G'));

		bot.clickButtons(IDialogConstants.OK_LABEL);
	}

}
