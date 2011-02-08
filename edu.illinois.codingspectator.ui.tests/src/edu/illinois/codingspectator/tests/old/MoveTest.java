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
abstract public class MoveTest extends CodingSpectatorTest {

	protected static final String MOVE_MENU_ITEM_NAME= "Move...";

	@Override
	protected String refactoringMenuItemName() {
		return MOVE_MENU_ITEM_NAME;
	}

}
