package edu.illinois.codingspectator.tests;

/**
 * 
 * This class only attempts to cancel the refactoring (not perform). It is based on the scenario
 * described in Issue #144.
 * 
 * @author Mohsen Vakilian, nchen
 * 
 */
public class InvalidExtractSuperclassTest extends ExtractSuperclassTest {

	private static final String TEST_FILE_NAME= "InvalidExtractSuperclassTestFile";

	@Override
	protected void configureRefactoringToCancel() {
		super.configureRefactoringToCancel();
		bot.textWithLabel(SUPERCLASS_NAME_LABEL).setText(TEST_FILE_NAME);
	}

	@Override
	protected void configureRefactoringToPerform() {
		configureRefactoringToCancel();
	}

	@Override
	public void verifyPerformedRefactoringBehavior() {
		verifyCanceledRefactoringBehavior();
	}

	protected String[] getRefactoringDialogPerformButtonSequence() {
		return getRefactoringDialogCancelButtonSequence();
	}

	@Override
	protected void selectElementToRefactor() {
		selectElementToRefactor(9, 6, "Child".length());
	}

	@Override
	String getTestFileName() {
		return TEST_FILE_NAME;
	}

}
