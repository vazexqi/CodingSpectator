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
public abstract class ExtractSuperclassTest extends CodingSpectatorTest {

	private static final String EXTRACT_SUPERCLASS_DIALOG_NAME= "Refactoring";

	protected static final String SUPERCLASS_NAME_LABEL= "Superclass name:";

	private static final String EXTRACT_SUPERCLASS_MENU_ITEM= "Extract Superclass...";

	@Override
	protected String getRefactoringDialogName() {
		return EXTRACT_SUPERCLASS_DIALOG_NAME;
	}

	@Override
	protected String refactoringMenuItemName() {
		return EXTRACT_SUPERCLASS_MENU_ITEM;
	}

}
