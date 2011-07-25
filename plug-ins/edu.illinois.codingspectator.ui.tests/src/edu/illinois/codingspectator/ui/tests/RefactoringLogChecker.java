/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;

import edu.illinois.codingspectator.refactorings.parser.CapturedRefactoringDescriptor;
import edu.illinois.codingspectator.refactorings.parser.RefactoringLog;

/**
 * 
 * @author Mohsen Vakilian
 * 
 */
public class RefactoringLogChecker extends AbstractLogChecker {

	private String projectName;

	private RefactoringLog expectedRefactoringLog;

	private RefactoringLog actualRefactoringLog;

	public RefactoringLogChecker(RefactoringLog.LogType logType, String refactoringKind, String testName, String projectName) {
		this.projectName= projectName;
		actualRefactoringLog= new RefactoringLog(logType);
		expectedRefactoringLog= RefactoringLogUtils.getExpectedRefactoringLog(refactoringKind + "/" + testName + "/" + RefactoringLog.toString(logType));
	}

	@Override
	public void assertActualLogIsEmpty() {
		assertFalse(String.format("Did not expect %s to exist.", actualRefactoringLog.getPathToRefactoringHistoryFolder()), actualLogExists());
	}

	@Override
	public void assertMatch() {
		Collection<CapturedRefactoringDescriptor> actualRefactoringDescriptors= RefactoringLogUtils.getRefactoringDescriptors(actualRefactoringLog, projectName);
		Collection<CapturedRefactoringDescriptor> expectedRefactoringDescriptors= RefactoringLogUtils.getRefactoringDescriptors(expectedRefactoringLog, projectName);
		assertEquals(expectedRefactoringDescriptors.size(), actualRefactoringDescriptors.size());

		Iterator<CapturedRefactoringDescriptor> actualIterator= actualRefactoringDescriptors.iterator();
		Iterator<CapturedRefactoringDescriptor> expectedIterator= expectedRefactoringDescriptors.iterator();

		while (expectedIterator.hasNext() && actualIterator.hasNext()) {
			DescriptorComparator.assertMatches(expectedIterator.next(), actualIterator.next());
		}
	}

	@Override
	public void clean() throws CoreException {
		actualRefactoringLog.delete();
	}

	@Override
	protected void copyActualLogsAsExpectedLogs() throws CoreException {
		if (actualLogExists() && !expectedLogExists()) {
			actualRefactoringLog.copy(expectedRefactoringLog);
		}
	}

	@Override
	protected void deleteExpectedLogs() throws CoreException {
		expectedRefactoringLog.delete();
	}

	@Override
	public boolean actualLogExists() {
		return actualRefactoringLog.exists();
	}

	@Override
	protected boolean expectedLogExists() {
		return expectedRefactoringLog.exists();
	}

}
