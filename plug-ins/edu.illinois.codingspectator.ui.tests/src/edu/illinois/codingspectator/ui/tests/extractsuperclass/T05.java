/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.extractsuperclass;

import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class T05 extends RefactoringTest {

	private static final String EXTRACT_SUPERCLASS_MENU_ITEM= "Extract Superclass...";

	protected static final String SUPERCLASS_NAME_LABEL= "Superclass name:";

	private final static String SELECTION= "methodToBePulledUp";

	private final static String NEW_SUPERCLASS_NAME= "NewSuperClassName";

	@Override
	protected String getTestFileName() {
		return "ExtractSuperclassTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 11, 16, SELECTION.length());
		bot.invokeRefactoringFromMenu(EXTRACT_SUPERCLASS_MENU_ITEM);
		bot.fillTextField(SUPERCLASS_NAME_LABEL, NEW_SUPERCLASS_NAME);
		bot.clickButtons(IDialogConstants.NEXT_LABEL, IDialogConstants.NEXT_LABEL, IDialogConstants.FINISH_LABEL);
	}

}
