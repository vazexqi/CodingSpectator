/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests.extractsuperclass;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
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
 * 
 */
public class UnavailableExtractSuperclassTest extends RefactoringTest {

	private static final String EXTRACT_SUPERCLASS_MENU_ITEM= "Extract Superclass...";

	private static final String SELECTION= "UnavailableExtractSuperclassTestFile";

	RefactoringLog refactoringLog= new RefactoringLog(RefactoringLog.LogType.UNAVAILABLE);

	@Override
	protected String getTestFileName() {
		return "UnavailableExtractSuperclassTestFile";
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
		bot.selectElementToRefactor(getTestFileFullName(), 5, 17, SELECTION.length());
		bot.invokeRefactoringFromMenu(EXTRACT_SUPERCLASS_MENU_ITEM);
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
		assertEquals("org.eclipse.jdt.ui.extract.superclass", capturedDescriptor.getID());
		assertEquals(getProjectName(), capturedDescriptor.getProject());
		assertNull(capturedDescriptor.getElement());
		assertNull(capturedDescriptor.getName());
		assertFalse(capturedDescriptor.doesReference());
		assertEquals(SELECTION, capturedDescriptor.getSelection());
		assertEquals("177 36", capturedDescriptor.getSelectionOffset());
		assertEquals("To activate this refactoring, please select a non-binary non-inner class or the name of an instance method or field.", capturedDescriptor.getStatus());
		assertEquals("5ab5c15f40fe569ebcad30a57cd08651", Encryptor.toMD5(capturedDescriptor.getCodeSnippet()));
	}

	@Override
	protected void doCleanRefactoringHistory() throws CoreException {
		refactoringLog.clean();
	}

}
