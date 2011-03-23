/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.rename;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.RefactoringLog.LogType;
import edu.illinois.codingspectator.ui.tests.RefactoringLogChecker;
import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * 
 * This test checks that CodingSpectator records the user's attempt to invoke a rename refactoring
 * on an Java keyword.
 * 
 * @author Mohsen Vakilian
 * @author nchen
 */
public class T01 extends RefactoringTest {

	private static final String RENAME_MENU_ITEM= "Rename...";

	private static final String SELECTION= "class";

	@Override
	protected String getTestFileName() {
		return "RenameKeywordTestFile";
	}

	@Override
	protected Collection<RefactoringLogChecker> getRefactoringLogCheckers() {
		return Arrays.asList(new RefactoringLogChecker(LogType.UNAVAILABLE, getRefactoringKind(), getClass().getSimpleName(), getProjectName()));
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 5, 7, SELECTION.length());
		bot.invokeRefactoringFromMenu(RENAME_MENU_ITEM);
		bot.clickButtons(IDialogConstants.OK_LABEL);
	}

}
