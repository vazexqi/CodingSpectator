/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.rename;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;
import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.CapturedRefactoringDescriptor;
import edu.illinois.codingspectator.ui.tests.Encryptor;
import edu.illinois.codingspectator.ui.tests.Encryptor.EncryptionException;
import edu.illinois.codingspectator.ui.tests.RefactoringLog;
import edu.illinois.codingspectator.ui.tests.RefactoringTest;

/**
 * 
 * This test checks that CodingSpectator records the user's attempt to invoke a rename refactoring
 * on an Java keyword.
 * 
 * @author Mohsen Vakilian
 * @author nchen
 */
public class UnavailableRenameKeywordTest extends RefactoringTest {

	private static final String RENAME_MENU_ITEM= "Rename...";

	private static final String SELECTION= "class";

	RefactoringLog refactoringLog= new RefactoringLog(RefactoringLog.LogType.UNAVAILABLE);

	@Override
	protected String getTestFileName() {
		return "RenameKeywordTestFile";
	}

	@Override
	protected String getTestInputLocation() {
		return "rename";
	}

	@Override
	protected void doRefactoringLogShouldBeEmpty() {
		assertFalse(refactoringLog.exists());
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 5, 7, SELECTION.length());
		bot.invokeRefactoringFromMenu(RENAME_MENU_ITEM);
		bot.clickButtons(IDialogConstants.OK_LABEL);
	}

	@Override
	protected void doRefactoringShouldBeLogged() throws EncryptionException {
		assertTrue(refactoringLog.exists());
		Collection<JavaRefactoringDescriptor> refactoringDescriptors= refactoringLog.getRefactoringDescriptors(getProjectName());
		assertEquals(1, refactoringDescriptors.size());
		JavaRefactoringDescriptor descriptor= refactoringDescriptors.iterator().next();
		CapturedRefactoringDescriptor capturedDescriptor= new CapturedRefactoringDescriptor(descriptor);
		assertTrue(capturedDescriptor.getTimestamp() > 0);
		assertEquals("", capturedDescriptor.getComment());
		assertEquals("CODINGSPECTATOR: RefactoringDescriptor from an unavailable refactoring", capturedDescriptor.getDescription());
		assertEquals(0, capturedDescriptor.getFlags());
		assertEquals(IJavaRefactorings.RENAME_UNKNOWN_JAVA_ELEMENT, capturedDescriptor.getID());
		assertEquals(getProjectName(), capturedDescriptor.getProject());
		assertNull(capturedDescriptor.getElement());
		assertNull(capturedDescriptor.getName());
		assertFalse(capturedDescriptor.doesReference());
		assertEquals(SELECTION, capturedDescriptor.getSelectionText());
		assertEquals(String.format("167 %d", SELECTION.length()), capturedDescriptor.getSelectionInCodeSnippet());
		assertEquals("Operation unavailable on the current selection.\n" +
				"Select a Java project, source folder, resource, package, compilation unit, type, field, method, parameter or a local variable", capturedDescriptor.getStatus());
		assertEquals("fb11f9579c34707811fee7300323c9b9", Encryptor.toMD5(capturedDescriptor.getCodeSnippet()));
		assertFalse(capturedDescriptor.isInvokedByQuickAssist());
	}

	@Override
	protected void doCleanRefactoringHistory() throws CoreException {
		refactoringLog.clean();
	}

}
