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
abstract public class MoveStaticMemberTest extends CodingSpectatorTest {

	protected static final String DESTINATION_TYPE_LABEL_FORMAT= "Destination type for '%s':";

	protected static final String MOVE_MENU_ITEM_NAME= "Move...";

	protected static final String MOVE_STATIC_MEMBER_DIALOG_NAME= "Move Static Members";

	abstract protected String getDestinationType();

	abstract protected String getSelectedMember();

	@Override
	protected String getRefactoringDialogName() {
		return MOVE_STATIC_MEMBER_DIALOG_NAME;
	}

	@Override
	protected String refactoringMenuItemName() {
		return MOVE_MENU_ITEM_NAME;
	}

	@Override
	protected void configureRefactoring() {
		bot.comboBoxWithLabel(getDestinationLabel()).setText(getDestinationType());
	}

	private String getDestinationLabel() {
		return String.format(DESTINATION_TYPE_LABEL_FORMAT, getSelectedMember());
	}

}
