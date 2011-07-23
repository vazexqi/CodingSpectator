/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import edu.illinois.codingspectator.efs.EFSFile;
import edu.illinois.codingspectator.refactoringproblems.logger.ProblemChanges;
import edu.illinois.codingspectator.refactoringproblems.parser.RefactoringProblemsLogDeserializer;
import edu.illinois.codingspectator.refactoringproblems.parser.RefactoringProblemsParserException;
import edu.illinois.codingspectator.refactorings.parser.RefactoringLog;

/**
 * @author Mohsen Vakilian
 * @author Balaji Ambresh Rajkumar
 * 
 */
public class RefactoringProblemsChecker extends AbstractLogChecker {

	private final EFSFile expectedLogFile;

	private final EFSFile actualLogFile;

	public RefactoringProblemsChecker(IPath expectedLogPath) {
		this.expectedLogFile= new EFSFile(expectedLogPath);
		this.actualLogFile= new EFSFile(RefactoringLog.getRefactoringStorageLocation("refactorings").append(ProblemChanges.REFACTORING_PROBLEMS_LOG));
	}

	@Override
	public void assertActualLogIsEmpty() {
		assertFalse(actualLogExists());
	}

	private List<ProblemChanges> getProblemChanges(IPath refactoringLogPath) throws RefactoringProblemsParserException {
		return new RefactoringProblemsLogDeserializer(false).deserializeRefactoringProblemsLog(refactoringLogPath.toOSString());
	}

	@Override
	public void assertMatch() throws RefactoringProblemsParserException {
		List<ProblemChanges> expectedProblems= getProblemChanges(expectedLogFile.getPath());
		List<ProblemChanges> actualProblems= getProblemChanges(actualLogFile.getPath());
		assertEquals(expectedProblems, actualProblems);
	}

	@Override
	public void clean() throws CoreException {
		actualLogFile.delete();
	}

	@Override
	public void copyActualLogsAsExpectedLogs() throws CoreException {
		if (actualLogExists() && !expectedLogExists()) {
			actualLogFile.copyTo(expectedLogFile);
		}
	}

	@Override
	protected void deleteExpectedLogs() throws CoreException {
		expectedLogFile.delete();
	}

	@Override
	protected boolean actualLogExists() {
		return actualLogFile.exists();
	}

	@Override
	protected boolean expectedLogExists() {
		return expectedLogFile.exists();
	}

}
