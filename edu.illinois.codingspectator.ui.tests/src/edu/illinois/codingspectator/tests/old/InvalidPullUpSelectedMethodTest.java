/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.tests.old;

import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class InvalidPullUpSelectedMethodTest extends PullUpTest {

	@Override
	protected void selectElementToRefactor() {
		selectElementToRefactor(14, 9, "m".length());
	}

	@Override
	protected String getTestFileName() {
		return "InvalidPullUpMethodTestFile";
	}

	@Override
	protected void cancelRefactoring() {
		bot.button(NEXT_BUTTON_LABEL).click();
		bot.button(NEXT_BUTTON_LABEL).click();
		bot.button(CANCEL_BUTTON_LABEL).click();
	}

	@Override
	protected void performRefactoring() {
		bot.button(FINISH_BUTTON_LABEL).click();
		try {
			bot.button(OK_BUTTON_LABEL).click();
		} catch (WidgetNotFoundException exception) {
			// FIXME: On my machine i.e. Mac, the second dialog box does not appear so SWTBot can't click on OK.
			bot.button(FINISH_BUTTON_LABEL).click();
		}
	}

	@Override
	public void verifyPerformedRefactoringBehavior() {
		super.verifyPerformedRefactoringBehavior();
		reportProblemsWithTest("Pulling up a method to class that already has a method with the same signature produces an exception. We need to either record it as a cancelld refactoring or a performed refactoring with a severe status. See issue #22 for more details.");
	}
}
