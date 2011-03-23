/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.pullup;

import java.util.Arrays;
import java.util.Collection;
import org.eclipse.jface.dialogs.IDialogConstants;
import edu.illinois.codingspectator.ui.tests.RefactoringLog.LogType;
import edu.illinois.codingspectator.ui.tests.RefactoringLogChecker;
import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class T01 extends RefactoringTest {

	private static final String PULL_UP_MENU_ITEM= "Pull Up...";

	@Override
	protected String getTestFileName() {
		return "InvalidPullUpMethodTestFile";
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 14, 9, "m".length());
		bot.invokeRefactoringFromMenu(PULL_UP_MENU_ITEM);
		bot.clickButtons(IDialogConstants.NEXT_LABEL, IDialogConstants.NEXT_LABEL, IDialogConstants.CANCEL_LABEL);
		System.err
				.println("This test throws the following exceptions: java.lang.reflect.InvocationTargetException\nCaused by: java.lang.NullPointerException\nRoot exception:\njava.lang.NullPointerException");
	}

	@Override
	protected Collection<RefactoringLogChecker> getRefactoringLogCheckers() {
		return Arrays.asList(new RefactoringLogChecker(LogType.CANCELLED, getRefactoringKind(), getClass().getSimpleName(), getProjectName()));
	}

}
