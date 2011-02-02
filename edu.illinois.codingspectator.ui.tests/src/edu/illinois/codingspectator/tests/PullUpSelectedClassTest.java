/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.tests;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class PullUpSelectedClassTest extends PullUpTest {

	@Override
	protected void selectElementToRefactor() {
		selectElementToRefactor(9, 6, "Child".length());
	}

	@Override
	protected String[] getRefactoringDialogPerformButtonSequence() {
		return new String[] { NEXT_BUTTON_LABEL, FINISH_BUTTON_LABEL };
	}

	@Override
	protected void configureRefactoringToPerform() {
		super.configureRefactoringToPerform();
		bot.button("Select All").click();
	}

}
