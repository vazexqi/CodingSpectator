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
public class ValidMoveStaticFieldTest extends MoveStaticMemberTest {

	@Override
	protected String getDestinationType() {
		return "edu.illinois.codingspectator.C2";
	}

	@Override
	protected String getSelectedMember() {
		return "field";
	}

	@Override
	public void selectElementToRefactor() {
		selectElementToRefactor(7, 18, 24 - 19);
	}

}
