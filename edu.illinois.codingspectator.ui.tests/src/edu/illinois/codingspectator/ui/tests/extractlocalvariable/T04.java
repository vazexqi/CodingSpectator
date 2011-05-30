/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.extractlocalvariable;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;

import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * 
 * This test invokes the extract local variable refactoring on an unsaved file.
 * 
 * @author Mohsen Vakilian
 * 
 */
public class T04 extends RefactoringTest {

	private static final String MENU_ITEM= "Extract Local Variable...";

	private static final String TEST_FILE_NAME= "ExtractLocalVariableTestFile";

	private static final String TEXT_TO_INSERT= "RefactoringOnUnsavedFile";

	private static final int SELECTION_LENGTH= "\"Test\"".length() + TEXT_TO_INSERT.length();

	@Override
	protected String getTestFileName() {
		return TEST_FILE_NAME;
	}

	@Override
	protected void doExecuteRefactoring() {
		final SWTBotEclipseEditor textEditor= bot.getTextEditor(getTestFileFullName());
		textEditor.insertText(8, 32, TEXT_TO_INSERT);
		bot.getBot().waitUntil(new DefaultCondition() {

			@Override
			public boolean test() throws Exception {
				return textEditor.getText().contains(TEXT_TO_INSERT);
			}

			@Override
			public String getFailureMessage() {
				return "Failed to insert text into the editor.";
			}
		});
		bot.selectElementToRefactor(getTestFileFullName(), 8, 27, SELECTION_LENGTH);
		bot.invokeRefactoringFromMenu(MENU_ITEM);
		bot.clickButtons(IDialogConstants.OK_LABEL);
		textEditor.save();
	}

}
