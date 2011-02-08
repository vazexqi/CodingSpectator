/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.tests;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

/**
 * @author Balaji Ambresh Rajkumar
 */
public class RenameJavaProjectTest extends CodingSpectatorTest {

	/**
	 * The project name has to be static since the new project name should be retained as long as
	 * the test is running i.e. till closeCurrentProject() completes.
	 */
	private static String newProjectName;

	@Override
	protected String refactoringMenuItemName() {
		return "Rename...";
	}

	@Override
	public void selectElementToRefactor() {
		SWTBotTreeItem sourceFolderTreeItem= selectCurrentJavaProject().getTreeItem(getProjectName());
		sourceFolderTreeItem.select();
	}

	@Override
	protected void configureRefactoringToPerform() {
		super.configureRefactoringToPerform();
		configureRefactoring();
	};

	protected void configureRefactoring() {
		newProjectName= "renamed-" + bot.textWithLabel("New name:").getText();
		bot.textWithLabel("New name:").setText(newProjectName);
	}

	/**
	 * Always return the current project name. The project name changes as part of this refactoring.
	 */
	@Override
	public String getProjectName() {
		if (newProjectName == null) {
			return super.getProjectName();
		}
		return newProjectName;
	}

	@Override
	protected String getRefactoringDialogName() {
		return "Rename Java Project";
	}

	@Override
	public void canSetupProject() throws Exception {
		super.canCreateANewJavaProject();
	}

	@Override
	String getTestFileName() {
		return null;
	}
}
