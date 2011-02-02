/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.tests;

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
	protected String[] getRefactoringDialogPerformButtonSequence() {
		return new String[] { FINISH_BUTTON_LABEL };
	}

	@Override
	public void selectElementToRefactor() {
		SWTBotTreeItem compilationUnitTreeItem= selectCurrentJavaProject().getTreeItem(getProjectName()).expand().expandNode("src").expandNode("edu.illinois.codingspectator");
		compilationUnitTreeItem.select(getTestFileFullName());
	}

	@Override
	protected void configureRefactoringToPerform() {
		super.configureRefactoringToPerform();
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
