/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests;

import static org.junit.Assert.assertFalse;

import org.eclipse.core.runtime.CoreException;

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
	public void assertLogIsEmpty() {
		assertFalse(String.format("Did not expect %s to exist.", actualRefactoringLog.getPathToRefactoringHistoryFolder()), actualLogExists());
	}

	@Override
	public void assertMatch() {
		CapturedRefactoringDescriptor actualRefactoringDescriptor= RefactoringLogUtils.getTheSingleRefactoringDescriptor(actualRefactoringLog, projectName);
		CapturedRefactoringDescriptor expectedRefactoringDescriptor= RefactoringLogUtils.getTheSingleRefactoringDescriptor(expectedRefactoringLog, projectName);
		DescriptorComparator.assertMatches(expectedRefactoringDescriptor, actualRefactoringDescriptor);
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
	protected boolean actualLogExists() {
		return actualRefactoringLog.exists();
	}

	@Override
	protected boolean expectedLogExists() {
		return expectedRefactoringLog.exists();
	}

}
