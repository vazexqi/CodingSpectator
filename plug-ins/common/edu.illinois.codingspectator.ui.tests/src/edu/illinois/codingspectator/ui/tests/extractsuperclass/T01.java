/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.extractsuperclass;

import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * 
 * This class only attempts to cancel the refactoring. It is based on the scenario described in
 * issue #144.
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class T01 extends RefactoringTest {

	private static final String EXTRACT_SUPERCLASS_MENU_ITEM= "Extract Superclass...";

	protected static final String SUPERCLASS_NAME_LABEL= "Superclass name:";

	private final static String SELECTION= "Child";

	private String getNewSuperClassName() {
		return getTestFileName();
	}

	@Override
	protected String getTestFileName() {
		return "InvalidExtractSuperclassTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 9, 6, SELECTION.length());
		bot.invokeRefactoringFromMenu(EXTRACT_SUPERCLASS_MENU_ITEM);
		bot.fillTextField(SUPERCLASS_NAME_LABEL, getNewSuperClassName());
		bot.clickButtons(IDialogConstants.CANCEL_LABEL);
	}

}
