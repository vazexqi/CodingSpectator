/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.extractmethod;

import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;

import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * 
 * This test quits the refactoring by pressing the ESC key.
 * 
 * @author Mohsen Vakilian
 * 
 */
public class T07 extends RefactoringTest {

	protected static final String EXTRACT_METHOD_MENU_ITEM_NAME= "Extract Method...";

	private static final String SELECTION= "System.out.println(\"main\");";

	@Override
	protected String getTestFileName() {
		return "ValidExtractMethodTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 8, 8, SELECTION.length());
		bot.invokeRefactoringFromMenu(EXTRACT_METHOD_MENU_ITEM_NAME);
		bot.activateShellWithName("Extract Method");
		bot.getBot().activeShell().pressShortcut(Keystrokes.ESC);
	}

}
