/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.tests.old;

import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.runner.RunWith;

/**
 * @author Mohsen Vakilian
 * @author nchen
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class InlineConstantTest extends CodingSpectatorTest {

	private static final String INLINE_CONSTANT_DIALOG_NAME= "Inline Constant";

	private static final String INLINE_CONSTANT_MENU_ITEM= "Inline...";

	static final String TEST_FILE_NAME= "InlineConstantTestFile";

	@Override
	protected String getRefactoringDialogName() {
		return INLINE_CONSTANT_DIALOG_NAME;
	}

	@Override
	public void selectElementToRefactor() {
		selectElementToRefactor(7, 24, 32 - 24);
	}

	@Override
	String getTestFileName() {
		return TEST_FILE_NAME;
	}

	@Override
	protected String refactoringMenuItemName() {
		return INLINE_CONSTANT_MENU_ITEM;
	}

}
