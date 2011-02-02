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
public abstract class ValidExtractSuperclassTest extends ExtractSuperclassTest {
	static final String TEST_FILE_NAME= "ExtractSuperclassTestFile";

	@Override
	public void selectElementToRefactor() {
		selectElementToRefactor(11, 16, "methodToBePulledUp".length());
	}

	@Override
	protected void configureRefactoringToPerform() {
		super.configureRefactoringToPerform();
		bot.textWithLabel(SUPERCLASS_NAME_LABEL).setText(getTestFileName() + "Parent");
	}

	@Override
	String getTestFileName() {
		return TEST_FILE_NAME;
	}

}
