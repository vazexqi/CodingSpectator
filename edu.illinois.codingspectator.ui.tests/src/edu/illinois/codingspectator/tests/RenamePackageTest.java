/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.tests;

import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.junit.runner.RunWith;

/**
 * @author Balaji Ambresh Rajkumar
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class RenamePackageTest extends CodingSpectatorTest {

	private static final String RENAME_PACKAGE_DIALOG_NAME= "Rename Package";

	private static final String RENAME_PACKAGE_MENU_ITEM= "Rename...";

	static final String TEST_FILE_NAME= "RenamePackageTestFile";

	@Override
	protected String getRefactoringDialogName() {
		return RENAME_PACKAGE_DIALOG_NAME;
	}

	@Override
	String getTestFileName() {
		return TEST_FILE_NAME;
	}

	@Override
	public void selectElementToRefactor() {
		SWTBotTreeItem packageTreeItem= selectCurrentJavaProject().getTreeItem(getProjectName()).expand().expandNode("src");
		packageTreeItem.select("edu.illinois.codingspectator");
	}

	@Override
	protected void configureRefactoringToPerform() {
		super.configureRefactoringToPerform();
		configureRefactoring();
	};

	@Override
	protected void configureRefactoringToCancel() {
		super.configureRefactoringToCancel();
		configureRefactoring();
	}

	protected void configureRefactoring() {
		final String originalPackageName= bot.textWithLabel("New name:").getText();
		bot.textWithLabel("New name:").setText("renamed." + originalPackageName);
	}

	@Override
	protected String refactoringMenuItemName() {
		return RENAME_PACKAGE_MENU_ITEM;
	}

}
