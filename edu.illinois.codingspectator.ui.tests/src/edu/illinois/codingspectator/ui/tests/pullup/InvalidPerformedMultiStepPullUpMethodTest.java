/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.pullup;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;

import edu.illinois.codingspectator.ui.tests.RefactoringLog;
import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class InvalidPerformedMultiStepPullUpMethodTest extends RefactoringTest {

	private static final String PULL_UP_MENU_ITEM= "Pull Up...";

	RefactoringLog refactoringLog= new RefactoringLog(RefactoringLog.LogType.PERFORMED);

	@Override
	protected String getTestFileName() {
		return "InvalidPullUpMethodTestFile";
	}

	@Override
	protected void doRefactoringLogShouldBeEmpty() {
		assertFalse(refactoringLog.exists());
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 14, 9, "m".length());
		bot.invokeRefactoringFromMenu(PULL_UP_MENU_ITEM);
		bot.clickButtons(IDialogConstants.FINISH_LABEL);
		try {
			bot.clickButtons(IDialogConstants.OK_LABEL);
		} catch (WidgetNotFoundException exception) {
			// FIXME: On my machine i.e. Mac, the second dialog box does not appear so SWTBot can't click on OK.
			bot.clickButtons(IDialogConstants.FINISH_LABEL);
		}
		System.err
				.println("This test throws the following exceptions: java.lang.reflect.InvocationTargetException\nCaused by: java.lang.NullPointerException\nRoot exception:\njava.lang.NullPointerException");
	}

	@Override
	protected void doRefactoringShouldBeLogged() {
		assertTrue(refactoringLog.exists());
	}

	@Override
	protected void doCleanRefactoringHistory() throws CoreException {
		refactoringLog.clean();
	}

}
