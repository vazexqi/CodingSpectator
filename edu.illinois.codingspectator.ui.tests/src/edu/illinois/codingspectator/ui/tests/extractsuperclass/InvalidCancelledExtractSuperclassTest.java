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
 * This class only attempts to cancel the refactoring. It is based on the scenario described in
 * issue #144.
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class InvalidCancelledExtractSuperclassTest extends RefactoringTest {

	private static final String EXTRACT_SUPERCLASS_MENU_ITEM= "Extract Superclass...";

	protected static final String SUPERCLASS_NAME_LABEL= "Superclass name:";

	private final static String SELECTION= "Child";

	RefactoringLog refactoringLog= new RefactoringLog(RefactoringLog.LogType.CANCELLED);

	private String getSelectedClassFullyQualifiedName() {
		return CodingSpectatorBot.PACKAGE_NAME + "." + SELECTION;
	}

	private String getNewSuperClassName() {
		return getTestFileName();
	}

	private String getNewSuperClassFullyQualifiedName() {
		return CodingSpectatorBot.PACKAGE_NAME + "." + getTestFileName();
	}

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
		bot.selectElementToRefactor(getTestFileFullName(), 9, 6, SELECTION.length());
		bot.invokeRefactoringFromMenu(EXTRACT_SUPERCLASS_MENU_ITEM);
		bot.fillTextField(SUPERCLASS_NAME_LABEL, getNewSuperClassName());
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
		assertEquals(String.format("Extract superclass '%s' from '%s'\n" +
				"- Original project: '%s'\n" +
				"- Original element: '%s'\n" +
				"- Sub types:\n" +
				"     %s\n" +
				"- Extracted class: '%s'\n" +
				"- Use super type where possible", getNewSuperClassFullyQualifiedName(), getSelectedClassFullyQualifiedName(), getProjectName(), getSelectedClassFullyQualifiedName(),
				getSelectedClassFullyQualifiedName(), getNewSuperClassFullyQualifiedName()),
				capturedDescriptor.getComment());
		assertEquals(String.format("/src<%s{%s[%s", CodingSpectatorBot.PACKAGE_NAME, getTestFileFullName(), SELECTION), capturedDescriptor.getInput());
		assertEquals(String.format("Extract superclass '%s'", getNewSuperClassName()), capturedDescriptor.getDescription());
		assertEquals(589830, capturedDescriptor.getFlags());
		assertEquals(IJavaRefactorings.EXTRACT_SUPERCLASS, capturedDescriptor.getID());
		assertEquals(getProjectName(), capturedDescriptor.getProject());
		assertNull(capturedDescriptor.getElement());
		assertNull(capturedDescriptor.getSelection());
	}

	private void attributesSpecificToExtractSuperclassShouldBeCorrect(CapturedRefactoringDescriptor capturedDescriptor) {
		assertEquals(getNewSuperClassName(), capturedDescriptor.getName());
		assertTrue(capturedDescriptor.getReplace());
		assertFalse(capturedDescriptor.getInstanceOf());
		assertTrue(capturedDescriptor.getStubs());
		assertEquals(0, capturedDescriptor.getExtract());
		assertEquals(0, capturedDescriptor.getDelete());
		assertEquals(0, capturedDescriptor.getIntegerAbstract());
		assertEquals(1, capturedDescriptor.getTypes());
		assertEquals(String.format("/src<%s{%s[%s", CodingSpectatorBot.PACKAGE_NAME, getTestFileFullName(), SELECTION), capturedDescriptor.getElement(1));
	}

	private void codingspectatorAttributesShouldBeCorrect(CapturedRefactoringDescriptor capturedDescriptor) throws EncryptionException {
		assertEquals(SELECTION, capturedDescriptor.getSelectionText());
		assertNull(capturedDescriptor.getSelectionInCodeSnippet());
		assertEquals(String.format("<FATALERROR\n" +
				"	\n" +
				"FATALERROR: Compilation unit '%s' already exists\n" +
				"Context: <Unspecified context>\n" +
				"code: none\n" +
				"Data: null\n" +
				">", getTestFileFullName()), capturedDescriptor.getStatus());
		assertEquals("3138c3e9935e3184e6f9da225852f0dc", Encryptor.toMD5(capturedDescriptor.getCodeSnippet()));
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
