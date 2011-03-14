/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.extractconstant;

import static org.hamcrest.text.pattern.Patterns.anyCharacterInCategory;
import static org.hamcrest.text.pattern.Patterns.oneOrMore;
import static org.hamcrest.text.pattern.Patterns.sequence;
import static org.hamcrest.text.pattern.Patterns.text;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.hamcrest.text.pattern.PatternComponent;
import org.hamcrest.text.pattern.PatternMatcher;

import edu.illinois.codingspectator.ui.tests.CapturedRefactoringDescriptor;
import edu.illinois.codingspectator.ui.tests.CodingSpectatorBot;
import edu.illinois.codingspectator.ui.tests.Encryptor;
import edu.illinois.codingspectator.ui.tests.Encryptor.EncryptionException;
import edu.illinois.codingspectator.ui.tests.RefactoringLog;
import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * @author Mohsen Vakilian
 * @author nchen
 */
public class CancelledExtractConstantTest extends RefactoringTest {

	private static final String EXTRACT_CONSTANT_MENU_ITEM= "Extract Constant...";

	static final String TEST_FILE_NAME= "ExtractConstantTestFile";

	private static final String SELECTION= "\"Test0\"";

	private static final String NEW_CONSTANT_NAME= "TEST0";

	RefactoringLog refactoringLog= new RefactoringLog(RefactoringLog.LogType.CANCELLED);

	@Override
	protected String getTestFileName() {
		return TEST_FILE_NAME;
	}

	@Override
	protected String getTestInputLocation() {
		return "extract-constant";
	}

	@Override
	protected void doRefactoringLogShouldBeEmpty() {
		assertFalse(refactoringLog.exists());
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 8, 27, SELECTION.length());
		bot.invokeRefactoringFromMenu(EXTRACT_CONSTANT_MENU_ITEM);
		bot.clickButtons(IDialogConstants.CANCEL_LABEL);
	}

	@Override
	protected void doRefactoringShouldBeLogged() throws EncryptionException {
		assertTrue(refactoringLog.exists());
		Collection<JavaRefactoringDescriptor> refactoringDescriptors= refactoringLog.getRefactoringDescriptors(getProjectName());
		assertEquals(1, refactoringDescriptors.size());
		JavaRefactoringDescriptor descriptor= refactoringDescriptors.iterator().next();
		CapturedRefactoringDescriptor capturedDescriptor= new CapturedRefactoringDescriptor(descriptor);
		capturedRefactoringDescriptorShouldBeCorrect(capturedDescriptor);
		codingspectatorAttributesShouldBeCorrect(capturedDescriptor);
	}

	private void codingspectatorAttributesShouldBeCorrect(CapturedRefactoringDescriptor capturedDescriptor) throws EncryptionException {
		assertEquals(SELECTION, capturedDescriptor.getSelectionText());
		assertEquals(String.format("272 %d", SELECTION.length()), capturedDescriptor.getSelectionInCodeSnippet());
		assertEquals("<OK\n>", capturedDescriptor.getStatus());
		assertEquals("ef03a6850277ef0f1c7cfcd0c6a663ef", Encryptor.toMD5(capturedDescriptor.getCodeSnippet()));
		assertFalse(capturedDescriptor.isInvokedByQuickAssist());
		PatternComponent timestampPattern= oneOrMore(anyCharacterInCategory("Digit"));
		PatternMatcher expectedNavigationHistoryPatternMatcher= new PatternMatcher(sequence(text("{[Extract Constant,BEGIN_REFACTORING,"), timestampPattern, text("],[TextInputPage,Cancel,"),
				timestampPattern, text("],}")));
		assertThat(capturedDescriptor.getNavigationHistory(), expectedNavigationHistoryPatternMatcher);
	}

	private void capturedRefactoringDescriptorShouldBeCorrect(CapturedRefactoringDescriptor capturedDescriptor) throws EncryptionException {
		javaAttributesShouldBeCorrect(capturedDescriptor);
		attributesSpecificToExtractConstantShouldBeCorrect(capturedDescriptor);
	}

	private void javaAttributesShouldBeCorrect(CapturedRefactoringDescriptor capturedDescriptor) {
		assertTrue(capturedDescriptor.getTimestamp() > 0);
		assertEquals(String.format("Extract constant '%s' from expression '%s'\n", NEW_CONSTANT_NAME, SELECTION) +
				String.format("- Original project: '%s'\n", getProjectName()) +
				String.format("- Constant name: '%s'\n", NEW_CONSTANT_NAME) +
				String.format("- Constant expression: '%s'\n", SELECTION) +
				"- Declared visibility: 'private'\n" +
				"- Replace occurrences of expression with constant", capturedDescriptor.getComment());
		assertEquals(String.format("/src<%s{%s", CodingSpectatorBot.PACKAGE_NAME, getTestFileFullName()), capturedDescriptor.getInput());
		assertEquals(String.format("Extract constant '%s'", NEW_CONSTANT_NAME), capturedDescriptor.getDescription());
		assertEquals(786432, capturedDescriptor.getFlags());
		assertEquals(IJavaRefactorings.EXTRACT_CONSTANT, capturedDescriptor.getID());
		assertEquals(getProjectName(), capturedDescriptor.getProject());
		assertNull(capturedDescriptor.getElement());
		assertEquals(String.format("272 %d", SELECTION.length()), capturedDescriptor.getSelection());
	}

	private void attributesSpecificToExtractConstantShouldBeCorrect(CapturedRefactoringDescriptor capturedDescriptor) {
		assertTrue(capturedDescriptor.getReplace());
		assertFalse(capturedDescriptor.getQualify());
		assertEquals(2, capturedDescriptor.getVisibility());
	}

	@Override
	protected void doCleanRefactoringHistory() throws CoreException {
		refactoringLog.clean();
	}

}
