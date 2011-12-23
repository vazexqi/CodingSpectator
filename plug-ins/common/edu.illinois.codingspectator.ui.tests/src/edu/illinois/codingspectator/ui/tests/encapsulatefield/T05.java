/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.encapsulatefield;

import static org.eclipse.swtbot.swt.finder.waits.Conditions.shellIsActive;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

import edu.illinois.codingspectator.ui.tests.CodingSpectatorBot;
import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * 
 * This test invokes an encapsulate field refactoring using a structured selection, then changes the
 * selection of a radio box and performs the refactoring.
 * 
 * @author Mohsen Vakilian
 * 
 */
public class T05 extends RefactoringTest {

	private static final String MENU_ITEM= "Encapsulate Field...";

	private static final String SELECTION= "field";

	@Override
	protected String getTestFileName() {
		return "EncapsulateFieldTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectFromPackageExplorer(getProjectName(), "src", CodingSpectatorBot.PACKAGE_NAME, getTestFileFullName(), getTestFileName(), SELECTION);
		bot.invokeRefactoringFromMenu(MENU_ITEM);
		final String dialogName= "Encapsulate Field";
		bot.getBot().waitUntilWidgetAppears(shellIsActive(dialogName));
		SWTBotShell shell= bot.getBot().shell(dialogName);
		bot.deselectRadio("use setter and getter");
		bot.getBot().radio("keep field reference").click();
		bot.clickButtons(IDialogConstants.OK_LABEL);
		bot.getBot().waitUntil(Conditions.shellCloses(shell));
	}

}
