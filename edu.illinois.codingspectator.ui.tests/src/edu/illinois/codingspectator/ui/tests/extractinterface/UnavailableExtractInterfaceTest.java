package edu.illinois.codingspectator.ui.tests.extractinterface;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;
import org.eclipse.jface.dialogs.IDialogConstants;

import edu.illinois.codingspectator.ui.tests.Encryptor;
import edu.illinois.codingspectator.ui.tests.Encryptor.EncryptionException;
import edu.illinois.codingspectator.ui.tests.CapturedRefactoringDescriptor;
import edu.illinois.codingspectator.ui.tests.RefactoringLog;
import edu.illinois.codingspectator.ui.tests.RefactoringTest;

public class UnavailableExtractInterfaceTest extends RefactoringTest {

	protected static final String EXTRACT_INTERFACE_ITEM_NAME= "Extract Interface...";

	private final static String SELECTION= "";

	RefactoringLog refactoringLog= new RefactoringLog(RefactoringLog.LogType.UNAVAILABLE);

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
		assertFalse(refactoringLog.exists());
	}

	@Override
	protected void doExecuteRefactoring() {
		bot.selectElementToRefactor(getTestFileFullName(), 17, 1, SELECTION.length());
		bot.invokeRefactoringFromMenu(EXTRACT_INTERFACE_ITEM_NAME);
		bot.clickButtons(IDialogConstants.OK_LABEL);
	}

	@Override
	protected void doRefactoringShouldBeLogged() throws EncryptionException {
		assertTrue(refactoringLog.exists());
		Collection<JavaRefactoringDescriptor> refactoringDescriptors= refactoringLog.getRefactoringDescriptors(getProjectName());
		assertEquals(1, refactoringDescriptors.size());
		JavaRefactoringDescriptor descriptor= refactoringDescriptors.iterator().next();
		CapturedRefactoringDescriptor refactoringDescriptorParser= new CapturedRefactoringDescriptor(descriptor);
		assertTrue(refactoringDescriptorParser.getTimestamp() > 0);
		assertEquals("", refactoringDescriptorParser.getComment());
		assertEquals("CODINGSPECTATOR: RefactoringDescriptor from an unavailable refactoring", refactoringDescriptorParser.getDescription());
		assertEquals(0, refactoringDescriptorParser.getFlags());
		assertEquals("org.eclipse.jdt.ui.extract.interface", refactoringDescriptorParser.getID());
		assertEquals(getProjectName(), refactoringDescriptorParser.getProject());
		assertNull(refactoringDescriptorParser.getElement());
		assertNull(refactoringDescriptorParser.getName());
		assertFalse(refactoringDescriptorParser.doesReference());
		assertEquals(SELECTION, refactoringDescriptorParser.getSelection());
		assertEquals("332 0", refactoringDescriptorParser.getSelectionOffset());
		assertEquals("To activate this refactoring, please select the name of a top level type.", refactoringDescriptorParser.getStatus());
		assertEquals("509e14617a2628706da3cb61b4c8cb93", Encryptor.toMD5(refactoringDescriptorParser.getCodeSnippet()));
		assertFalse(refactoringDescriptorParser.isInvokedByQuickAssist());
	}

	@Override
	protected void doCleanRefactoringHistory() throws CoreException {
		refactoringLog.clean();
	}

}
