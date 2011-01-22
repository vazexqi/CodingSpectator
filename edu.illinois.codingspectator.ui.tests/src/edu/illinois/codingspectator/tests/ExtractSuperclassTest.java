/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.tests;

import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.runner.RunWith;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class ExtractSuperclassTest extends CodingSpectatorTest {
	// This is an unusual title for the dialog box - maybe JDT forgot to set it properly
	private static final String EXTRACT_SUPERCLASS_DIALOG_NAME= "Refactoring";

	private static final String SUPERCLASS_NAME_LABEL= "Superclass name:";

	private static final String EXTRACT_SUPERCLASS_MENU_ITEM= "Extract Superclass...";

	static final String TEST_FILE_NAME= "ExtractSuperclassTestFile";

	@Override
	protected String getRefactoringDialogName() {
		return EXTRACT_SUPERCLASS_DIALOG_NAME;
	}

	@Override
	public void selectElementToRefactor() {
		selectElementToRefactor(11, 16, 34 - 16);
	}

	@Override
	protected void configureRefactoring() {
		super.configureRefactoring();
		bot.textWithLabel(SUPERCLASS_NAME_LABEL).setText(getTestFileName() + "Parent");
	}

	@Override
	String getTestFileName() {
		return TEST_FILE_NAME;
	}

	@Override
	protected String refactoringMenuItemName() {
		return EXTRACT_SUPERCLASS_MENU_ITEM;
	}

	protected String[] getRefactoringDialogApplyButtonSequence() {
		return new String[] { FINISH_BUTTON_LABEL };
	}

	// Currently ExtractSuperclass Refactoring is not captured. This test is to verify 
	// that we did not break anything in the UI while we were instrumenting the other refactorings.

	@Override
	public void verifyCanceledRefactoringBehavior() {
		// Do nothing
	}

	@Override
	public void verifyPerformedRefactoringBehavior() {
		// Do nothing
	}


}
