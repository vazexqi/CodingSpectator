/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.extractmethod;

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
public class InvalidPerformedExtractMethodTest extends RefactoringTest {

	protected static final String EXTRACT_METHOD_MENU_ITEM_NAME= "Extract Method...";

	private static final String SELECTION= "System.out.println(\"main\");";

	private static final String METHOD_NAME= "invalidExtractedMethod";

	RefactoringLog performedRefactoringLog= new RefactoringLog(RefactoringLog.LogType.PERFORMED);

	RefactoringLog eclipseRefactoringLog= new RefactoringLog(RefactoringLog.LogType.ECLIPSE);

	@Override
	protected String getTestFileName() {
		return "InvalidExtractMethodTestFile";
	}

	@Override
	protected String getTestInputLocation() {
		return "extract-method";
	}

	@Override
	protected void doRefactoringLogShouldBeEmpty() {
		assertFalse(performedRefactoringLog.exists());
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 8, 8, SELECTION.length());
		bot.invokeRefactoringFromMenu(EXTRACT_METHOD_MENU_ITEM_NAME);
		bot.fillTextField("Method name:", METHOD_NAME);
		bot.clickButtons(IDialogConstants.OK_LABEL, CodingSpectatorBot.CONTINUE_LABEL);
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
		assertEquals(String.format("255 %d", SELECTION.length()), capturedDescriptor.getSelectionInCodeSnippet());
		assertEquals(
				"<ERROR\n"
						+
						"	\n"
						+
						"ERROR: Method 'invalidExtractedMethod' already exists in type 'InvalidExtractMethodTestFile'.\n"
						+
						"Context: [Working copy] InvalidExtractMethodTestFile.java [in edu.illinois.codingspectator [in src [in TestProject_class edu.illinois.codingspectator.ui.tests.extractmethod.InvalidPerformedExtractMethodTest]]]\n"
						+
						"  package edu.illinois.codingspectator\n" +
						"  class InvalidExtractMethodTestFile\n" +
						"    static void main(String[])\n" +
						"    void invalidExtractedMethod()\n" +
						"code: none\n" +
						"Data: null\n" +
						">", capturedDescriptor.getStatus());
		assertEquals("fb31a59dcee7936e67df667e2af8121b", Encryptor.toMD5(capturedDescriptor.getCodeSnippet()));
		assertFalse(capturedDescriptor.isInvokedByQuickAssist());
		PatternComponent timestampPattern= oneOrMore(anyCharacterInCategory("Digit"));
		PatternMatcher expectedNavigationHistoryPatternMatcher= new PatternMatcher(sequence(text("{[Extract Method,BEGIN_REFACTORING,"), timestampPattern, text("],[ExtractMethodInputPage,OK,"),
				timestampPattern, text("],[ErrorPage,Con&tinue,"), timestampPattern, text("],}")));
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
		attributesSpecificToExtractMethodShouldBeCorrect(capturedDescriptor);
	}

	private void javaAttributesShouldBeCorrect(CapturedRefactoringDescriptor capturedDescriptor) {
		assertTrue(capturedDescriptor.getTimestamp() > 0);
		assertEquals(
				String.format("Extract method 'private static void %s()' from '%s.%s.main()' to 'edu.illinois.codingspectator.%s'\n", METHOD_NAME, CodingSpectatorBot.PACKAGE_NAME, getTestFileName(),
						getTestFileName())
						+
						String.format("- Original project: '%s'\n", getProjectName()) +
						String.format("- Method name: '%s'\n", METHOD_NAME) +
						String.format("- Destination type: '%s.%s'\n", CodingSpectatorBot.PACKAGE_NAME, getTestFileName()) +
						"- Declared visibility: 'private'", capturedDescriptor.getComment());
		assertEquals(String.format("/src<%s{%s", CodingSpectatorBot.PACKAGE_NAME, getTestFileFullName()), capturedDescriptor.getInput());
		assertEquals(String.format("Extract method '%s'", METHOD_NAME), capturedDescriptor.getDescription());
		assertEquals(786434, capturedDescriptor.getFlags());
		assertEquals(IJavaRefactorings.EXTRACT_METHOD, capturedDescriptor.getID());
		assertEquals(getProjectName(), capturedDescriptor.getProject());
		assertNull(capturedDescriptor.getElement());
		assertEquals(String.format("255 %d", SELECTION.length()), capturedDescriptor.getSelection());
	}

	private void attributesSpecificToExtractMethodShouldBeCorrect(CapturedRefactoringDescriptor capturedDescriptor) {
		assertEquals(METHOD_NAME, capturedDescriptor.getName());
		assertFalse(capturedDescriptor.doesReference());
		assertFalse(capturedDescriptor.getReplace());
		assertEquals(2, capturedDescriptor.getVisibility());
		assertFalse(capturedDescriptor.getComments());
		assertEquals(0, capturedDescriptor.getDestination());
		assertFalse(capturedDescriptor.getExceptions());
	}

	@Override
	protected void doCleanRefactoringHistory() throws CoreException {
		performedRefactoringLog.clean();
		eclipseRefactoringLog.clean();
	}

}
