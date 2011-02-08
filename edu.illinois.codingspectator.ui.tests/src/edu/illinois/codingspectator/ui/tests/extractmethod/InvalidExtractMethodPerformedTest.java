/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.extractmethod;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.CodingSpectatorBot;
import edu.illinois.codingspectator.ui.tests.RefactoringLog;
import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class InvalidExtractMethodPerformedTest extends RefactoringTest {

	protected static final String EXTRACT_METHOD_MENU_ITEM_NAME= "Extract Method...";

	RefactoringLog refactoringLog= new RefactoringLog(RefactoringLog.LogType.PERFORMED);

	@Override
	protected String getTestFileName() {
		return "InvalidExtractMethodTestFile";
	}

	@Override
	protected String getTestInputLocation() {
		return "extract-method";
	}

	@Override
	protected void doRefactoringLogShouldBeEmpty() {
		assertFalse(refactoringLog.exists());
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileName(), 8, 8, "System.out.println(\"main\");".length());
		bot.invokeRefactoringFromMenu(EXTRACT_METHOD_MENU_ITEM_NAME);
		bot.fillTextField("Method name:", "invalidExtractedMethod");
		bot.clickButtons(IDialogConstants.OK_LABEL, CodingSpectatorBot.CONTINUE_LABEL);
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
