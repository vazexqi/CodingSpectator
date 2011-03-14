/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.inline;

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
public class PerformedInlineConstantTest extends RefactoringTest {

	private static final String INLINE_CONSTANT_MENU_ITEM= "Inline...";

	private static final String SELECTION= "CONSTANT";

	RefactoringLog performedRefactoringLog= new RefactoringLog(RefactoringLog.LogType.PERFORMED);

	RefactoringLog eclipseRefactoringLog= new RefactoringLog(RefactoringLog.LogType.ECLIPSE);

	@Override
	protected String getTestFileName() {
		return "InlineConstantTestFile";
	}

	@Override
	protected String getTestInputLocation() {
		return "inline";
	}

	@Override
	protected void doRefactoringLogShouldBeEmpty() {
		assertFalse(performedRefactoringLog.exists());
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 7, 24, SELECTION.length());
		bot.invokeRefactoringFromMenu(INLINE_CONSTANT_MENU_ITEM);
		bot.clickButtons(IDialogConstants.OK_LABEL);
	}

	@Override
	protected void doRefactoringShouldBeLogged() throws EncryptionException {
		performedLogShouldBeCorrect();
		eclipseLogShouldBeCorrect();
	}

	private void performedLogShouldBeCorrect() throws EncryptionException {
		assertTrue(performedRefactoringLog.exists());
		Collection<JavaRefactoringDescriptor> refactoringDescriptors= performedRefactoringLog.getRefactoringDescriptors(getProjectName());
		assertEquals(1, refactoringDescriptors.size());
		JavaRefactoringDescriptor descriptor= refactoringDescriptors.iterator().next();
		CapturedRefactoringDescriptor capturedDescriptor= new CapturedRefactoringDescriptor(descriptor);
		capturedRefactoringDescriptorShouldBeCorrect(capturedDescriptor);
		codingspectatorAttributesShouldBeCorrect(capturedDescriptor);
	}

	private void codingspectatorAttributesShouldBeCorrect(CapturedRefactoringDescriptor capturedDescriptor) throws EncryptionException {
		assertEquals(SELECTION, capturedDescriptor.getSelectionText());
		assertEquals(String.format("223 %d", SELECTION.length()), capturedDescriptor.getSelectionInCodeSnippet());
		assertEquals("<OK\n>", capturedDescriptor.getStatus());
		assertEquals("8532f1d6de06a9519645fde01f082b3b", Encryptor.toMD5(capturedDescriptor.getCodeSnippet()));
		assertFalse(capturedDescriptor.isInvokedByQuickAssist());
		PatternComponent timestampPattern= oneOrMore(anyCharacterInCategory("Digit"));
		PatternMatcher expectedNavigationHistoryPatternMatcher= new PatternMatcher(sequence(text("{[Inline Constant,BEGIN_REFACTORING,"), timestampPattern, text("],[InlineConstantInputPage,OK,"),
				timestampPattern, text("],}")));
		assertThat(capturedDescriptor.getNavigationHistory(), expectedNavigationHistoryPatternMatcher);
	}

	private void eclipseLogShouldBeCorrect() throws EncryptionException {
		assertTrue(eclipseRefactoringLog.exists());
		Collection<JavaRefactoringDescriptor> refactoringDescriptors= eclipseRefactoringLog.getRefactoringDescriptors(getProjectName());
		assertEquals(1, refactoringDescriptors.size());
		JavaRefactoringDescriptor descriptor= refactoringDescriptors.iterator().next();
		CapturedRefactoringDescriptor capturedDescriptor= new CapturedRefactoringDescriptor(descriptor);
		capturedRefactoringDescriptorShouldBeCorrect(capturedDescriptor);
	}

	private void capturedRefactoringDescriptorShouldBeCorrect(CapturedRefactoringDescriptor capturedDescriptor) throws EncryptionException {
		javaAttributesShouldBeCorrect(capturedDescriptor);
		attributesSpecificToInlineConstantShouldBeCorrect(capturedDescriptor);
	}

	private void javaAttributesShouldBeCorrect(CapturedRefactoringDescriptor capturedDescriptor) {
		assertTrue(capturedDescriptor.getTimestamp() > 0);
		assertEquals(String.format("Inline constant '%s.%s.%s' in '%s.%s'\n", CodingSpectatorBot.PACKAGE_NAME, getTestFileName(), SELECTION, CodingSpectatorBot.PACKAGE_NAME, getTestFileName()) +
				String.format("- Original project: '%s'\n", getProjectName()) +
				String.format("- Original element: '%s.%s.%s'\n", CodingSpectatorBot.PACKAGE_NAME, getTestFileName(), SELECTION) +
				"- Remove constant declaration\n" +
				"- Replace all references to constant with expression", capturedDescriptor.getComment());
		assertEquals(String.format("/src<%s{%s", CodingSpectatorBot.PACKAGE_NAME, getTestFileFullName()), capturedDescriptor.getInput());
		assertEquals(String.format("Inline constant '%s'", SELECTION), capturedDescriptor.getDescription());
		assertEquals(786438, capturedDescriptor.getFlags());
		assertEquals(IJavaRefactorings.INLINE_CONSTANT, capturedDescriptor.getID());
		assertEquals(getProjectName(), capturedDescriptor.getProject());
		assertNull(capturedDescriptor.getElement());
		assertEquals(String.format("223 %d", SELECTION.length()), capturedDescriptor.getSelection());
	}

	private void attributesSpecificToInlineConstantShouldBeCorrect(CapturedRefactoringDescriptor capturedDescriptor) {
		assertTrue(capturedDescriptor.getReplace());
		assertTrue(capturedDescriptor.getRemove());
	}

	@Override
	protected void doCleanRefactoringHistory() throws CoreException {
		performedRefactoringLog.clean();
		eclipseRefactoringLog.clean();
	}

}
