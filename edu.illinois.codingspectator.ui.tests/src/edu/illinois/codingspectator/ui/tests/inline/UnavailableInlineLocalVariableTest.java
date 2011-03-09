/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.inline;

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
 * If a final field is not initialized, the compiler will report an error. If the user tries to
 * inline an uninitialized local variable, an dialog will show up saying that the refactoring is not
 * allowed on an the selected variable. This test checks that the user's attempt to invoke a
 * refactoring on an uninitialized local variable gets captured properly.
 * 
 * @author Mohsen Vakilian
 * @author nchen
 */
public class UnavailableInlineLocalVariableTest extends RefactoringTest {

	private static final String INLINE_MENU_ITEM= "Inline...";

	private static final String SELECTION= "localVariable";

	RefactoringLog refactoringLog= new RefactoringLog(RefactoringLog.LogType.UNAVAILABLE);

	@Override
	protected String getTestFileName() {
		return "UninitializedLocalVariableTestFile";
	}

	@Override
	protected String getTestInputLocation() {
		return "inline";
	}

	@Override
	protected void doRefactoringLogShouldBeEmpty() {
		assertFalse(refactoringLog.exists());
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 8, 15, SELECTION.length());
		bot.invokeRefactoringFromMenu(INLINE_MENU_ITEM);
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
		assertEquals(IJavaRefactorings.INLINE_LOCAL_VARIABLE, capturedDescriptor.getID());
		assertEquals(getProjectName(), capturedDescriptor.getProject());
		assertNull(capturedDescriptor.getElement());
		assertNull(capturedDescriptor.getName());
		assertFalse(capturedDescriptor.doesReference());
		assertEquals(SELECTION, capturedDescriptor.getSelection());
		assertEquals(String.format("271 %d", SELECTION.length()), capturedDescriptor.getSelectionOffset());
		assertEquals(String.format("Local variable '%s' is not initialized at declaration.", SELECTION), capturedDescriptor.getStatus());
		assertEquals("af50f656a26fa3a1aa2171e36657bbf8", Encryptor.toMD5(capturedDescriptor.getCodeSnippet()));
		assertFalse(capturedDescriptor.isInvokedByQuickAssist());
	}

	@Override
	protected void doCleanRefactoringHistory() throws CoreException {
		refactoringLog.clean();
	}

}
