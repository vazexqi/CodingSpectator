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
public class InvalidMoveStaticMethodTest extends MoveStaticMemberTest {

	@Override
	protected String getDestinationType() {
		return "edu.illinois.codingspectator.C3";
	}

	@Override
	protected String getSelectedMember() {
		return "m()";
	}

	@Override
	public void selectElementToRefactor() {
		selectElementToRefactor(9, 16, 1);
	}

	@Override
	protected String[] getRefactoringDialogApplyButtonSequence() {
		return new String[] { OK_BUTTON_LABEL, CONTINUE_BUTTON_LABEL };
	}

}
