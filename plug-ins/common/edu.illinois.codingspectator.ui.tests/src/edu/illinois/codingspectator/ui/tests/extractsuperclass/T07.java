/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.extractsuperclass;

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

	private static final String EXTRACT_SUPERCLASS_MENU_ITEM= "Extract Superclass...";

	protected static final String SUPERCLASS_NAME_LABEL= "Superclass name:";

	private final static String SELECTION= "Child";

	private String getNewSuperClassName() {
		return getTestFileName();
	}

	@Override
	protected String getTestFileName() {
		return "InvalidExtractSuperclassTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 9, 6, SELECTION.length());
		bot.invokeRefactoringFromMenu(EXTRACT_SUPERCLASS_MENU_ITEM);
		bot.fillTextField(SUPERCLASS_NAME_LABEL, getNewSuperClassName());
		bot.activateShellWithName("Refactoring").pressShortcut(Keystrokes.ESC);
	}

}
