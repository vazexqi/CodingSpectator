/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.tests.old;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class RenameSourceFolderTest extends CodingSpectatorTest {

	@Override
	protected String refactoringMenuItemName() {
		return "Rename...";
	}

	@Override
	public void selectElementToRefactor() {
		SWTBotTreeItem sourceFolderTreeItem= selectCurrentJavaProject().getTreeItem(getProjectName()).expand().expandNode("src");
		sourceFolderTreeItem.select();
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
		bot.textWithLabel("New name:").setText("renamed-src");
	}

	@Override
	String getTestFileName() {
		return "RenameSourceFolderUnitTestFile";
	}

	@Override
	protected String getRefactoringDialogName() {
		return "Rename Source Folder";
	}

}
