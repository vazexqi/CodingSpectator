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
public class ValidExtractMethodTest extends ExtractMethodTest {

	private static final String VALID_EXTRACTED_METHOD_NAME= "validExtractedMethod";

	static final String TEST_FILE_NAME= "ValidExtractMethodTestFile";

	@Override
	protected String getExtractedMethodName() {
		return VALID_EXTRACTED_METHOD_NAME;
	}

	@Override
	public void prepareRefactoring() {
		invokeRefactoring(7, 8, 37 - 8);
	}

	@Override
	String getProjectNameSuffix() {
		return ValidExtractMethodTest.class.toString();
	}

	@Override
	String getTestFileName() {
		return TEST_FILE_NAME;
	}

}
