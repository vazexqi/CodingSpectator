/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.extractmethod;

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
public class ValidCanceledExtractMethodTest extends RefactoringTest {

	protected static final String EXTRACT_METHOD_MENU_ITEM_NAME= "Extract Method...";

	private static final String SELECTION= "System.out.println(\"main\");";

	@Override
	protected String getTestFileName() {
		return "ValidExtractMethodTestFile";
	}

	@Override
	protected String getTestInputLocation() {
		return "extract-method";
	}

	@Override
	protected Collection<RefactoringLogChecker> getRefactoringLogCheckers() {
		return Arrays.asList(new RefactoringLogChecker(LogType.CANCELLED, getTestInputLocation(), getClass().getSimpleName(), getProjectName()));
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 8, 8, SELECTION.length());
		bot.invokeRefactoringFromMenu(EXTRACT_METHOD_MENU_ITEM_NAME);
		bot.clickButtons(IDialogConstants.CANCEL_LABEL);
	}

}
