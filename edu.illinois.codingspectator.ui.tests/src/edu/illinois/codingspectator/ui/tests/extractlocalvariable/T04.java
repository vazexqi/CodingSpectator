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

	private static final String INIITIAL_STRING_LITERAL= "\"Test";

	private static final String TEXT_TO_INSERT= "RefactoringOnUnsavedFile";

	private static final String SELECTION= INIITIAL_STRING_LITERAL + TEXT_TO_INSERT + "\"";

	@Override
	protected String getTestFileName() {
		return TEST_FILE_NAME;
	}

	@Override
	protected void doExecuteRefactoring() {
		final SWTBotEclipseEditor textEditor= bot.getTextEditor(getTestFileFullName());
		bot.getBot().waitUntil(new DefaultCondition() {

			@Override
			public boolean test() throws Exception {
				return textEditor.getText().contains(INIITIAL_STRING_LITERAL);
			}

			@Override
			public String getFailureMessage() {
				return "Failed to set the contents of the editor.";
			}
		});
		textEditor.insertText(8, 32, TEXT_TO_INSERT);
		bot.getBot().waitUntil(new DefaultCondition() {

			@Override
			public boolean test() throws Exception {
				return textEditor.getText().contains(SELECTION);
			}

			@Override
			public String getFailureMessage() {
				return "Failed to insert text into the editor.";
			}
		});
		bot.selectElementToRefactor(getTestFileFullName(), 8, 27, SELECTION.length());
		bot.invokeRefactoringFromMenu(MENU_ITEM);
		bot.clickButtons(IDialogConstants.OK_LABEL);
		textEditor.save();
	}

}
