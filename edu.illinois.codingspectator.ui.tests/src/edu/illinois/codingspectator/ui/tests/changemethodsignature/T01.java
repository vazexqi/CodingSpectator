/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.changemethodsignature;

import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * 
 * Test changes the signature of a method by adding a parameter to it.
 * 
 * @author Mohsen Vakilian
 * 
 */
public class T01 extends RefactoringTest {

	private static final String MENU_ITEM= "Change Method Signature...";

	@Override
	protected String getTestFileName() {
		return "ChangeMethodSignatureTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectFromPackageExplorer(getProjectName(), "src", "edu.illinois.codingspectator", getTestFileFullName(), "C", "m1(Object) : void");
		bot.invokeRefactoringFromMenu(MENU_ITEM);
		bot.clickButtons("Add", IDialogConstants.OK_LABEL);
	}

}
