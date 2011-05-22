/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.inlinelocalvariable;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.junit.Ignore;

import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * 
 * This test checks that an inline refactoring performed via quick assist is captured correctly.
 * 
 * @author Mohsen Vakilian
 * 
 */
@Ignore("See issue #142.")
public class T02 extends RefactoringTest {

	private static final String MENU_ITEM= "Inline local variable";

	private static final String SELECTION= "localVariable";

	@Override
	protected String getTestFileName() {
		return "InlineLocalVariableTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 8, 15, SELECTION.length());
		SWTWorkbenchBot swtBot= bot.getBot();
		swtBot.activeEditor().toTextEditor().quickfix(MENU_ITEM);
	}

}
