/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.tests;

import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

public class ValidMoveCusTest extends MoveTest {

	@Override
	protected void selectElementToRefactor() {
		SWTBotTreeItem compilationUnitTreeItem= selectCurrentJavaProject().getTreeItem(getProjectName()).expand().expandNode("src").expandNode("edu.illinois.codingspectator");
		compilationUnitTreeItem.select(getTestFileFullName());
	}

	@Override
	protected void configureRefactoringToPerform() {
		super.configureRefactoringToPerform();
		bot.button("Create Package...").click();
		bot.textWithLabel("Name:").setText("edu.illinois.codingspectator.subpackage");
		bot.button("Finish").click();
	}

	@Override
	String getTestFileName() {
		return "MoveCusTestFile";
	}

	@Override
	protected String getRefactoringDialogName() {
		return "Move";
	}

}
