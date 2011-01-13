/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.tests;

import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.runner.RunWith;


/**
 * @author Mohsen Vakilian
 * @author nchen
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class ExtractConstantTest extends CodingSpectatorTest {

	private static final String EXTRACT_CONSTANT_MENU_ITEM= "Extract Constant...";

	private static final String EXTRACT_CONSTANT_DIALOG_NAME= "Extract Constant";

	static final String TEST_FILE_NAME= "ExtractConstantTestFile";

	@Override
	protected String getRefactoringDialogName() {
		return EXTRACT_CONSTANT_DIALOG_NAME;
	}

	@Override
	public void selectElementToRefactor() {
		selectElementToRefactor(8, 27, 34 - 27);
	}

	protected String refactoringMenuItemName() {
		return EXTRACT_CONSTANT_MENU_ITEM;
	}

	@Override
	String getTestFileName() {
		return TEST_FILE_NAME;
	}

}
