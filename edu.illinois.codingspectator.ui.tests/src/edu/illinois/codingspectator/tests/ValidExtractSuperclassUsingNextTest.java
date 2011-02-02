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
public class ValidExtractSuperclassUsingNextTest extends ValidExtractSuperclassTest {

	protected String[] getRefactoringDialogPerformButtonSequence() {
		return new String[] { NEXT_BUTTON_LABEL, NEXT_BUTTON_LABEL, FINISH_BUTTON_LABEL };
	}

	protected String[] getRefactoringDialogCancelButtonSequence() {
		return new String[] { NEXT_BUTTON_LABEL, NEXT_BUTTON_LABEL, CANCEL_BUTTON_LABEL };
	}

	protected void configureRefactoringToCancel() {
		super.configureRefactoringToCancel();
		configureRefactoringToPerform();
	}

}
