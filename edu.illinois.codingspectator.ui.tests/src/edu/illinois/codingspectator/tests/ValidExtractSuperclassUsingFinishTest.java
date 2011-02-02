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
public class ValidExtractSuperclassUsingFinishTest extends ValidExtractSuperclassTest {

	protected String[] getRefactoringDialogPerformButtonSequence() {
		return new String[] { FINISH_BUTTON_LABEL };
	}

}
