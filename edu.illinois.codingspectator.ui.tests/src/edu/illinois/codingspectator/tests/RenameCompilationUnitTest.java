/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.tests;

import static org.junit.Assert.assertTrue;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class RenameCompilationUnitTest extends CodingSpectatorTest {

	@Override
	protected String refactoringMenuItemName() {
		return "Rename...";
	}


	@Override
	protected String getRefactoringDialogApplyButtonName() {
		return FINISH_BUTTON_NAME;
	}

	@Override
	public void prepareRefactoring() {
		SWTBotTreeItem compilationUnitTreeItem= selectCurrentJavaProject().getTreeItem(getProjectName()).expand().expandNode("src").expandNode("edu.illinois.codingspectator");
		compilationUnitTreeItem.select(getTestFileFullName());
		SWTBotMenu refactorMenu= bot.menu(REFACTOR_MENU_NAME);
		assertTrue(refactorMenu.isEnabled());

		SWTBotMenu refactoringMenuItem= refactorMenu.menu(refactoringMenuItemName());
		assertTrue(refactoringMenuItem.isEnabled());

		refactoringMenuItem.click();
		bot.textWithLabel("New name:").setText("Renamed" + getTestFileName());
	}

	@Override
	String getTestFileName() {
		return "RenameCompilationUnitTestFile";
	}

	@Override
	protected String getRefactoringDialogName() {
		return "Rename Compilation Unit";
	}

}
