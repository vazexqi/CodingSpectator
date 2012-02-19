/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.infertypearguments;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

import edu.illinois.codingspectator.ui.tests.CodingSpectatorBot;
import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * 
 * This test invokes an infer type arguments refactoring from a textual selection, configures it and
 * performs it. Then, it invokes and reverts the configuration and performs the same refactoring.
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
		//Because the refactoring tool saves the configuration of the input dialog for future invocations,
		//this test invokes the refactoring twice to revert the input page back to its original configuration. 
		configureAndPerformRefactoring();
		configureAndPerformRefactoring();
	}

	private void configureAndPerformRefactoring() {
		SWTBotShell shell;
		bot.selectFromPackageExplorer(getProjectName(), "src", CodingSpectatorBot.PACKAGE_NAME, getTestFileFullName());
		bot.invokeRefactoringFromMenu(MENU_ITEM);
		shell= bot.activateShellWithName("Infer Generic Type Arguments");
		bot.getBot().checkBox("Leave unconstrained type arguments raw (rather than inferring <?>)").click();
		bot.clickButtons(IDialogConstants.OK_LABEL);
		bot.waitUntil(Conditions.shellCloses(shell));
	}

}
