/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.extractsuperclass;

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
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class ValidCancelledMultiStepExtractSuperclassTest extends RefactoringTest {

	private static final String EXTRACT_SUPERCLASS_MENU_ITEM= "Extract Superclass...";

	protected static final String SUPERCLASS_NAME_LABEL= "Superclass name:";

	private final static String SELECTION= "methodToBePulledUp";

	private final static String NEW_SUPERCLASS_NAME= "NewSuperClassName";

	RefactoringLog refactoringLog= new RefactoringLog(RefactoringLog.LogType.CANCELLED);

	private String getSelectedClassName() {
		return getTestFileName();
	}

	private String getSelectedClassFullyQualifiedName() {
		return CodingSpectatorBot.PACKAGE_NAME + "." + getSelectedClassName();
	}

	@Override
	protected String getTestFileName() {
		return "ExtractSuperclassTestFile";
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
		bot.selectElementToRefactor(getTestFileFullName(), 11, 16, SELECTION.length());
		bot.invokeRefactoringFromMenu(EXTRACT_SUPERCLASS_MENU_ITEM);
		bot.fillTextField(SUPERCLASS_NAME_LABEL, NEW_SUPERCLASS_NAME);
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

	private void capturedRefactoringDescriptorShouldBeCorrect(CapturedRefactoringDescriptor capturedDescriptor) throws EncryptionException {
		javaAttributesShouldBeCorrect(capturedDescriptor);
		attributesSpecificToExtractSuperclassShouldBeCorrect(capturedDescriptor);
	}

	private void javaAttributesShouldBeCorrect(CapturedRefactoringDescriptor capturedDescriptor) {
		assertTrue(capturedDescriptor.getTimestamp() > 0);
		assertEquals(String.format("Extract superclass 'CodingSpectatorDefaultDestinationTypeName' from '%s'\n" +
				"- Original project: '%s'\n" +
				"- Original element: '%s'\n" +
				"- Sub types:\n" +
				"     %s\n" +
				"- Extracted class: 'CodingSpectatorDefaultDestinationTypeName'\n" +
				"- Extracted members:\n" +
				"     %s.%s()\n" +
				"- Use super type where possible", getSelectedClassFullyQualifiedName(), getProjectName(), getSelectedClassFullyQualifiedName(), getSelectedClassFullyQualifiedName(),
				getSelectedClassFullyQualifiedName(), SELECTION),
				capturedDescriptor.getComment());
		assertEquals(String.format("/src<%s{%s[%s", CodingSpectatorBot.PACKAGE_NAME, getTestFileFullName(), getSelectedClassName()), capturedDescriptor.getInput());
		assertEquals(String.format("Extract superclass '%s'", NEW_SUPERCLASS_NAME), capturedDescriptor.getDescription());
		assertEquals(589830, capturedDescriptor.getFlags());
		assertEquals(IJavaRefactorings.EXTRACT_SUPERCLASS, capturedDescriptor.getID());
		assertEquals(getProjectName(), capturedDescriptor.getProject());
		assertNull(capturedDescriptor.getElement());
		assertNull(capturedDescriptor.getSelection());
	}

	private void attributesSpecificToExtractSuperclassShouldBeCorrect(CapturedRefactoringDescriptor capturedDescriptor) {
		assertEquals(NEW_SUPERCLASS_NAME, capturedDescriptor.getName());
		assertTrue(capturedDescriptor.getReplace());
		assertFalse(capturedDescriptor.getInstanceOf());
		assertTrue(capturedDescriptor.getStubs());
		assertEquals(1, capturedDescriptor.getExtract());
		assertEquals(1, capturedDescriptor.getDelete());
		assertEquals(0, capturedDescriptor.getIntegerAbstract());
		assertEquals(1, capturedDescriptor.getTypes());
		assertEquals(String.format("/src<%s{%s[%s~%s", CodingSpectatorBot.PACKAGE_NAME, getTestFileFullName(), getSelectedClassName(), SELECTION), capturedDescriptor.getElement(1));
		assertEquals(String.format("/src<%s{%s[%s~%s", CodingSpectatorBot.PACKAGE_NAME, getTestFileFullName(), getSelectedClassName(), SELECTION), capturedDescriptor.getElement(2));
		assertEquals(String.format("/src<%s{%s[%s", CodingSpectatorBot.PACKAGE_NAME, getTestFileFullName(), getSelectedClassName()), capturedDescriptor.getElement(3));
	}

	private void codingspectatorAttributesShouldBeCorrect(CapturedRefactoringDescriptor capturedDescriptor) throws EncryptionException {
		System.err.println("The selection is not what the user has exactly selected.");
		assertEquals(getSelectedClassName(), capturedDescriptor.getSelectionText());
		assertNull(capturedDescriptor.getSelectionInCodeSnippet());
		assertEquals("<OK\n>", capturedDescriptor.getStatus());
		assertEquals("a0145c89c792aeb972395ada6007b545", Encryptor.toMD5(capturedDescriptor.getCodeSnippet()));
		assertFalse(capturedDescriptor.isInvokedByQuickAssist());
		PatternComponent timestampPattern= oneOrMore(anyCharacterInCategory("Digit"));
		PatternMatcher expectedNavigationHistoryPatternMatcher= new PatternMatcher(sequence(text("{[Extract Superclass,BEGIN_REFACTORING,"), timestampPattern,
				text("],[ExtractSupertypeMemberPage,Cancel,"), timestampPattern, text("],}")));
		assertThat(capturedDescriptor.getNavigationHistory(), expectedNavigationHistoryPatternMatcher);
	}

	@Override
	protected void doCleanRefactoringHistory() throws CoreException {
		refactoringLog.clean();
	}

}
