/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.encapsulatefield;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRadio;
import org.junit.Ignore;

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
@Ignore("See issue #275.")
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
//		bot.getBot().radio("keep field reference").click();
// I tried a combination of sleep, wait, setFocus and activateShell to communicate the change in the radio button. But, it didn't work.
		bot.activateShellWithName("Encapsulate Field");
		final SWTBotRadio radio= bot.getBot().radio("keep field reference");
		radio.setFocus();
		radio.click();
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				while (Display.getDefault().readAndDispatch())
					;
			}
		});
		bot.sleep();
		bot.getBot().waitUntil(new DefaultCondition() {

			@Override
			public boolean test() throws Exception {
				return radio.isSelected();
			}

			@Override
			public String getFailureMessage() {
				return radio.getText() + " did not get selected.";
			}
		});
		bot.getBot().button(IDialogConstants.OK_LABEL).setFocus();
		bot.clickButtons(IDialogConstants.OK_LABEL);
	}

}
