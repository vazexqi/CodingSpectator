/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.introduceparameterobject;

import org.eclipse.jdt.internal.ui.refactoring.RefactoringMessages;
import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.CodingSpectatorBot;
import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * 
 * This test invokes an introduce parameter object refactoring on an overriding method, deselects
 * one of the parameter, previews the changes and finally cancels the refactoring.
 * 
 * @author Mohsen Vakilian
 * 
 */
@SuppressWarnings("restriction")
public class T02 extends RefactoringTest {

	private static final String MENU_ITEM= "Introduce Parameter Object...";

	private static final String SELECTION= "m1";

	@Override
	protected String getTestFileName() {
		return "IntroduceParameterObjectTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 19, 9, SELECTION.length());
		bot.invokeRefactoringFromMenu(MENU_ITEM);
		bot.clickButtons(IDialogConstants.YES_LABEL);
		bot.getBot().tableInGroup(RefactoringMessages.IntroduceParameterObjectWizard_type_group).getTableItem(0).uncheck();
		bot.clickButtons(CodingSpectatorBot.PREVIEW_LABEL, IDialogConstants.CANCEL_LABEL);
	}

}
