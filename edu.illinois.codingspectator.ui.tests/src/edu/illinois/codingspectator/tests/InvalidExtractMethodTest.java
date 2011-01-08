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
public class InvalidExtractMethodTest extends ExtractMethodTest {

	private static final String CONTINUE_BUTTON_NAME= "Continue";

	private static final String INVALID_EXTRACTED_METHOD= "invalidExtractedMethod";

	static final String TEST_FILE_NAME= "InvalidExtractMethodTestFile";

	@Override
	protected String getExtractedMethodName() {
		return INVALID_EXTRACTED_METHOD;
	}

	@Override
	protected void performRefactoring() {
		super.performRefactoring();
		bot.button(CONTINUE_BUTTON_NAME).click();
	}

	@Override
	public void prepareRefactoring() {
		invokeRefactoring(8, 8, 35 - 8);
	}

	@Override
	String getProjectNameSuffix() {
		return InvalidExtractMethodTest.class.toString();
	}

	@Override
	String getTestFileName() {
		return TEST_FILE_NAME;
	}

}
