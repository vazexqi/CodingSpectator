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
public class InvalidPullUpSelectedMethodTest extends PullUpTest {

	@Override
	protected void selectElementToRefactor() {
		selectElementToRefactor(14, 9, "m".length());
	}

	@Override
	protected String[] getRefactoringDialogApplyButtonSequence() {
		return new String[] { FINISH_BUTTON_LABEL };
	}

	@Override
	protected String getTestFileName() {
		return "InvalidPullUpMethodTestFile";
	}

}
