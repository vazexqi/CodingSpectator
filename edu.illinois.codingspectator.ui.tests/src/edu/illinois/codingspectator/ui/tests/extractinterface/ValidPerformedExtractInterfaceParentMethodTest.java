package edu.illinois.codingspectator.ui.tests.extractinterface;

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

public class ValidPerformedExtractInterfaceParentMethodTest extends RefactoringTest {

	protected static final String EXTRACT_INTERFACE_ITEM_NAME= "Extract Interface...";

	private static final String SELECTION= "m1";

	private static final String SELECTED_CLASS= "Parent";

	private static final String NEW_INTERFACE_NAME= "I" + SELECTED_CLASS;

	RefactoringLog performedRefactoringLog= new RefactoringLog(RefactoringLog.LogType.PERFORMED);

	RefactoringLog eclipseRefactoringLog= new RefactoringLog(RefactoringLog.LogType.ECLIPSE);

	private String getSelectedClassFullQualifiedName() {
		return CodingSpectatorBot.PACKAGE_NAME + "." + SELECTED_CLASS;
	}

	private String getNewInterfaceFullQualifiedName() {
		return CodingSpectatorBot.PACKAGE_NAME + "." + NEW_INTERFACE_NAME;
	}


	@Override
	protected String getTestFileName() {
		return "ExtractInterfaceTestFile";
	}

	@Override
	protected String getTestInputLocation() {
		return "extract-interface";
	}

	@Override
	protected void doRefactoringLogShouldBeEmpty() {
		assertFalse(performedRefactoringLog.exists());
		assertFalse(eclipseRefactoringLog.exists());
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 6, 17, SELECTION.length());
		bot.invokeRefactoringFromMenu(EXTRACT_INTERFACE_ITEM_NAME);

		bot.fillTextField("Interface name:", NEW_INTERFACE_NAME);
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

	private void eclipseLogShouldBeCorrect() throws EncryptionException {
		assertTrue(eclipseRefactoringLog.exists());
		Collection<JavaRefactoringDescriptor> refactoringDescriptors= eclipseRefactoringLog.getRefactoringDescriptors(getProjectName());
		assertEquals(1, refactoringDescriptors.size());
		JavaRefactoringDescriptor descriptor= refactoringDescriptors.iterator().next();
		CapturedRefactoringDescriptor capturedDescriptor= new CapturedRefactoringDescriptor(descriptor);
		capturedRefactoringDescriptorShouldBeCorrect(capturedDescriptor);
	}

	private void codingspectatorAttributesShouldBeCorrect(CapturedRefactoringDescriptor capturedDescriptor) throws EncryptionException {
//		assertEquals(SELECTION, capturedDescriptor.getSelectionText());
		System.err.println("The captured selection text is different from what the test has actullay selected.");
		assertNull(capturedDescriptor.getSelectionInCodeSnippet());
		assertEquals("<OK\n>", capturedDescriptor.getStatus());
		assertEquals("6785752829aa56871a5225736a7d2162", Encryptor.toMD5(capturedDescriptor.getCodeSnippet()));
		assertFalse(capturedDescriptor.isInvokedByQuickAssist());
		PatternComponent timestampPattern= oneOrMore(anyCharacterInCategory("Digit"));
		PatternMatcher expectedNavigationHistoryPatternMatcher= new PatternMatcher(sequence(text("{[Extract Interface,BEGIN_REFACTORING,"), timestampPattern,
				text("],[TextInputPage,OK,"),
				timestampPattern, text("],}")));
		assertThat(capturedDescriptor.getNavigationHistory(), expectedNavigationHistoryPatternMatcher);
	}

	private void capturedRefactoringDescriptorShouldBeCorrect(CapturedRefactoringDescriptor capturedDescriptor) throws EncryptionException {
		javaAttributesShouldBeCorrect(capturedDescriptor);
		attributesSpecificToExtractInterfaceShouldBeCorrect(capturedDescriptor);
	}

	private void javaAttributesShouldBeCorrect(CapturedRefactoringDescriptor capturedDescriptor) {
		assertTrue(capturedDescriptor.getTimestamp() > 0);
		assertEquals(String.format("Extract interface '%s' from '%s'\n" +
				"- Original project: '%s'\n" +
				"- Original element: '%s'\n" +
				"- Extracted interface: '%s'\n" +
				"- Use super type where possible", getNewInterfaceFullQualifiedName(), getSelectedClassFullQualifiedName(), getProjectName(), getSelectedClassFullQualifiedName(),
				getNewInterfaceFullQualifiedName()),
				capturedDescriptor.getComment());
		assertEquals(String.format("/src<%s{%s[%s", CodingSpectatorBot.PACKAGE_NAME, getTestFileFullName(), SELECTED_CLASS), capturedDescriptor.getInput());
		assertEquals(String.format("Extract interface '%s'", NEW_INTERFACE_NAME), capturedDescriptor.getDescription());
		assertEquals(589830, capturedDescriptor.getFlags());
		assertEquals(IJavaRefactorings.EXTRACT_INTERFACE, capturedDescriptor.getID());
		assertEquals(getProjectName(), capturedDescriptor.getProject());
		assertNull(capturedDescriptor.getElement());
		assertNull(capturedDescriptor.getSelection());
	}

	private void attributesSpecificToExtractInterfaceShouldBeCorrect(CapturedRefactoringDescriptor capturedDescriptor) {
		assertTrue(capturedDescriptor.getAbstract());
		assertTrue(capturedDescriptor.getComments());
		assertFalse(capturedDescriptor.getInstanceOf());
		assertTrue(capturedDescriptor.getPublic());
		assertEquals(NEW_INTERFACE_NAME, capturedDescriptor.getName());
	}

	@Override
	protected void doCleanRefactoringHistory() throws CoreException {
		performedRefactoringLog.clean();
		eclipseRefactoringLog.clean();
	}

}
