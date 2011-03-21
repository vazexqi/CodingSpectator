/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.extractsuperclass;

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
public class ValidCancelledSingleStepExtractSuperclassTest extends RefactoringTest {

	private static final String EXTRACT_SUPERCLASS_MENU_ITEM= "Extract Superclass...";

	protected static final String SUPERCLASS_NAME_LABEL= "Superclass name:";

	private final static String SELECTION= "methodToBePulledUp";

	private final static String NEW_SUPERCLASS_NAME= "NewSuperClassName";

	@Override
	protected String getTestFileName() {
		return "ExtractSuperclassTestFile";
	}

	@Override
	protected String getTestInputLocation() {
		return "extract-superclass";
	}

	@Override
	protected Collection<RefactoringLogChecker> getRefactoringLogCheckers() {
		return Arrays.asList(new RefactoringLogChecker(LogType.CANCELLED, getTestInputLocation(), getClass().getSimpleName(), getProjectName()));
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 11, 16, SELECTION.length());
		bot.invokeRefactoringFromMenu(EXTRACT_SUPERCLASS_MENU_ITEM);
		bot.fillTextField(SUPERCLASS_NAME_LABEL, NEW_SUPERCLASS_NAME);
		bot.clickButtons(IDialogConstants.CANCEL_LABEL);
	}

	@Override
	protected void doRefactoringShouldBeLogged() {
		super.doRefactoringShouldBeLogged();
		System.err.println("The selection is not what the user has exactly selected.");
	}

}
