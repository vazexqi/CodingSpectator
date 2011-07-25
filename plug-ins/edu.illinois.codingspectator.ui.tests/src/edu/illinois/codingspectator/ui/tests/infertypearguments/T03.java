/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.infertypearguments;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * 
 * This test invokes an infer type arguments refactoring from a textual selection, configures it and
 * performs it.
 * 
 * @author Mohsen Vakilian
 * 
 */
public class T03 extends RefactoringTest {

	private static final String MENU_ITEM= "Infer Generic Type Arguments...";

	@Override
	protected String getTestFileName() {
		return "InferTypeArgumentsTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 8, 13, "InferTypeArgumentsTestFile".length());
		bot.invokeRefactoringFromMenu(MENU_ITEM);
		SWTBotShell shell= bot.activateShellWithName("Infer Generic Type Arguments");
		bot.getBot().checkBox("Leave unconstrained type arguments raw (rather than inferring <?>)").click();
		bot.clickButtons(IDialogConstants.OK_LABEL);
		bot.waitUntil(Conditions.shellCloses(shell));
	}

}
