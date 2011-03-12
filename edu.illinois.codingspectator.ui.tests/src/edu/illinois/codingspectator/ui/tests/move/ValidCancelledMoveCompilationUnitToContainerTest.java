/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.move;

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
import org.eclipse.jdt.core.refactoring.descriptors.MoveDescriptor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.hamcrest.text.pattern.PatternComponent;
import org.hamcrest.text.pattern.PatternMatcher;

import edu.illinois.codingspectator.ui.tests.CapturedRefactoringDescriptor;
import edu.illinois.codingspectator.ui.tests.CodingSpectatorBot;
import edu.illinois.codingspectator.ui.tests.Encryptor;
import edu.illinois.codingspectator.ui.tests.RefactoringLog;
import edu.illinois.codingspectator.ui.tests.RefactoringTest;
import edu.illinois.codingspectator.ui.tests.Encryptor.EncryptionException;

/**
 * @author Balaji Ambresh Rajkumar
 */
public class ValidCancelledMoveCompilationUnitToContainerTest extends RefactoringTest {

	private static final String TARGET_CONTAINER= ".settings";

	RefactoringLog refactoringLog= new RefactoringLog(RefactoringLog.LogType.CANCELLED);

	@Override
	protected String getTestFileName() {
		return "MoveCusTestFile";
	}

	@Override
	protected String getTestInputLocation() {
		return "move";
	}

	@Override
	protected void doRefactoringLogShouldBeEmpty() {
		assertFalse(refactoringLog.exists());
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectFromPackageExplorer(getProjectName(), "src", "edu.illinois.codingspectator", getTestFileFullName());
		bot.invokeRefactoringFromMenu("Move...");
		bot.activateShellWithName("Move");
		bot.getCurrentTree().pressShortcut(org.eclipse.jface.bindings.keys.KeyStroke.getInstance('.'));

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
		assertEquals(getTestFileFullName(), capturedDescriptor.getSelectionText());
		assertEquals("<INFO\n" + 
				"	\n" + 
				"INFO: Java references will not be updated.\n" + 
				"Context: <Unspecified context>\n" + 
				"code: none\n" + 
				"Data: null\n" + 
				">", capturedDescriptor.getStatus());
		assertEquals("36ae396c15ffa4c320b98197b46cbfb5", Encryptor.toMD5(capturedDescriptor.getCodeSnippet()));
		assertFalse(capturedDescriptor.isInvokedByQuickAssist());
		PatternComponent timestampPattern= oneOrMore(anyCharacterInCategory("Digit"));
		PatternMatcher expectedNavigationHistoryPatternMatcher= new PatternMatcher(sequence(text("{[Move,BEGIN_REFACTORING,"), timestampPattern, text("],[MoveInputPage,Cancel,"),
				timestampPattern, text("],}")));
		assertThat(capturedDescriptor.getNavigationHistory(), expectedNavigationHistoryPatternMatcher);
	}

	private void capturedRefactoringDescriptorShouldBeCorrect(CapturedRefactoringDescriptor capturedDescriptor) throws EncryptionException {
		javaAttributesShouldBeCorrect(capturedDescriptor);
		attributesSpecificToMoveCuToContainerShouldBeCorrect(capturedDescriptor);
	}

	private void javaAttributesShouldBeCorrect(CapturedRefactoringDescriptor capturedDescriptor) {
		assertTrue(capturedDescriptor.getTimestamp() > 0);
		assertEquals(String.format("Move element '%s' to '%s'\n" +
				"- Original project: '%s'\n" +
				"- Destination element: '%s'\n" +
				"- Original element: '%s.%s'\n" +
				"- Update references to refactored element", getTestFileFullName(), TARGET_CONTAINER, getProjectName(), TARGET_CONTAINER, CodingSpectatorBot.PACKAGE_NAME, getTestFileFullName())
				, capturedDescriptor.getComment());
		assertEquals("Move compilation unit", capturedDescriptor.getDescription());
		assertEquals(589830, capturedDescriptor.getFlags());
		assertEquals(IJavaRefactorings.MOVE, capturedDescriptor.getID());
		assertEquals(getProjectName(), capturedDescriptor.getProject());
		assertNull(capturedDescriptor.getElement());
	}

	private void attributesSpecificToMoveCuToContainerShouldBeCorrect(CapturedRefactoringDescriptor capturedDescriptor) {
		assertEquals(String.format("/src<%s{%s", CodingSpectatorBot.PACKAGE_NAME, getTestFileFullName()), capturedDescriptor.getElement(1));
		assertTrue(capturedDescriptor.doesReference());
		assertFalse(capturedDescriptor.getQualified());
		assertEquals(TARGET_CONTAINER, capturedDescriptor.getTarget());
		assertEquals(0, capturedDescriptor.getFiles());
		assertEquals(0, capturedDescriptor.getFolders());
		assertEquals(MoveDescriptor.POLICY_MOVE_RESOURCES, capturedDescriptor.getPolicy());
		assertEquals("*", capturedDescriptor.getPatterns());

	}

	@Override
	protected void doCleanRefactoringHistory() throws CoreException {
		refactoringLog.clean();
	}

}
