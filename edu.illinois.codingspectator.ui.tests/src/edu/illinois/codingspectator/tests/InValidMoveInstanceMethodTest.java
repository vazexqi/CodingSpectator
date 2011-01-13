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
public class InValidMoveInstanceMethodTest extends MoveInstanceMethodTest {

	@Override
	public void selectElementToRefactor() {
		selectElementToRefactor(11, 9, 2);
	}

}
