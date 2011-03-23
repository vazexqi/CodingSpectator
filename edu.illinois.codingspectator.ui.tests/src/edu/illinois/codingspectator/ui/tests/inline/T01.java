/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.inline;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.RefactoringLog.LogType;
import edu.illinois.codingspectator.ui.tests.RefactoringLogChecker;
import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * 
 * If a final field is not initialized, the compiler will report an error. If the user tries to
 * inline an uninitialized final static field, an dialog will show up saying that the refactoring is
 * not allowed on an the selected field. This test checks that the user's attempt to invoke a
 * refactoring on an uninitialized constant gets captured properly.
 * 
 * @author Mohsen Vakilian
 * @author nchen
 */
public class T01 extends RefactoringTest {

	private static final String INLINE_MENU_ITEM= "Inline...";

	private static final String SELECTION= "CONSTANT";

	@Override
	protected String getTestFileName() {
		return "UninitializedFinalTestFile";
	}

	@Override
	protected Collection<RefactoringLogChecker> getRefactoringLogCheckers() {
		return Arrays.asList(new RefactoringLogChecker(LogType.UNAVAILABLE, getRefactoringKind(), getClass().getSimpleName(), getProjectName()));
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 7, 24, SELECTION.length());
		bot.invokeRefactoringFromMenu(INLINE_MENU_ITEM);
		bot.clickButtons(IDialogConstants.OK_LABEL);
	}

}
