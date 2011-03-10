/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.pushdown;

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
 * @author Mohsen Vakilian
 * @author nchen
 * @author Balaji Ambresh Rajkumar
 * 
 */
public class UnavailablePushDownFieldTest extends RefactoringTest {

	private static final String PUSH_DOWN_MENU_ITEM= "Push Down...";

	private static final String SELECTION= "Child2";

	RefactoringLog refactoringLog= new RefactoringLog(RefactoringLog.LogType.UNAVAILABLE);

	@Override
	protected String getTestFileName() {
		return "PushDownSingleFieldTestFile";
	}

	@Override
	protected String getTestInputLocation() {
		return "push-down";
	}

	@Override
	protected void doRefactoringLogShouldBeEmpty() {
		assertFalse(refactoringLog.exists());
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 14, 6, SELECTION.length());
		bot.invokeRefactoringFromMenu(PUSH_DOWN_MENU_ITEM);
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
		assertEquals(IJavaRefactorings.PUSH_DOWN, capturedDescriptor.getID());
		assertEquals(getProjectName(), capturedDescriptor.getProject());
		assertNull(capturedDescriptor.getElement());
		assertNull(capturedDescriptor.getName());
		assertFalse(capturedDescriptor.doesReference());
		assertEquals(SELECTION, capturedDescriptor.getSelectionText());
		assertEquals("300 6", capturedDescriptor.getSelectionInCodeSnippet());
		assertEquals("To activate this refactoring, please select the name of a non-binary instance method or field.", capturedDescriptor.getStatus());
		assertEquals("ef78dac63bfd63f8a78d2e274433849e", Encryptor.toMD5(capturedDescriptor.getCodeSnippet()));
		assertFalse(capturedDescriptor.isInvokedByQuickAssist());
	}

	@Override
	protected void doCleanRefactoringHistory() throws CoreException {
		refactoringLog.clean();
	}

}
