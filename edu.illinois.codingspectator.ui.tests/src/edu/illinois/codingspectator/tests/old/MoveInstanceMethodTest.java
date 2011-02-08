/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.tests.old;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
abstract public class MoveInstanceMethodTest extends MoveTest {

	protected static final String MOVE_METHOD_DIALOG_NAME= "Move Method";

	@Override
	protected String getRefactoringDialogName() {
		return MOVE_METHOD_DIALOG_NAME;
	}

	@Override
	String getTestFileName() {
		return "MoveInstanceMethodTestFile";
	}

}
