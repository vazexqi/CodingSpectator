/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.extractsuperclass;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.RefactoringLog;
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
public class CancelledInvalidExtractSuperclassTest extends RefactoringTest {

	private static final String EXTRACT_SUPERCLASS_MENU_ITEM= "Extract Superclass...";

	protected static final String SUPERCLASS_NAME_LABEL= "Superclass name:";

	RefactoringLog refactoringLog= new RefactoringLog(RefactoringLog.LogType.CANCELLED);

	@Override
	protected String getTestFileName() {
		return "InvalidExtractSuperclassTestFile";
	}

	@Override
	protected String getTestInputLocation() {
		return "extract-superclass";
	}

	@Override
	protected void doRefactoringLogShouldBeEmpty() {
		assertFalse(refactoringLog.exists());
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileName(), 9, 6, "Child".length());
		bot.invokeRefactoringFromMenu(EXTRACT_SUPERCLASS_MENU_ITEM);
		bot.fillTextField(SUPERCLASS_NAME_LABEL, getTestFileName());
		bot.clickButtons(IDialogConstants.CANCEL_LABEL);
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
