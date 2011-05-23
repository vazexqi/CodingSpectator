/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import edu.illinois.codingspectator.refactoringproblems.logger.ProblemChanges;
import edu.illinois.codingspectator.refactoringproblems.parser.RefactoringProblemsLogDeserializer;
import edu.illinois.codingspectator.refactoringproblems.parser.RefactoringProblemsParserException;

/**
 * @author Mohsen Vakilian
 * @author Balaji Ambresh Rajkumar
 * 
 */
public class RefactoringProblemsChecker implements LogChecker {

	private final EFSFile expectedLogFile;

	private final EFSFile actualLogFile;

	public RefactoringProblemsChecker(IPath expectedLogPath) {
		this.expectedLogFile= new EFSFile(expectedLogPath);
		this.actualLogFile= new EFSFile(RefactoringLog.getRefactoringStorageLocation("refactorings").append(ProblemChanges.REFACTORING_PROBLEMS_LOG));
	}

	public void assertLogIsEmpty() {
		assertFalse(actualLogFile.exists());
	}

	private List<ProblemChanges> getProblemChanges(IPath refactoringLogPath) throws RefactoringProblemsParserException {
		return new RefactoringProblemsLogDeserializer(false).deserializeRefactoringProblemsLog(refactoringLogPath.toOSString());
	}

	public void assertMatch() throws RefactoringProblemsParserException {
		List<ProblemChanges> expectedProblems= getProblemChanges(expectedLogFile.getPath());
		List<ProblemChanges> actualProblems= getProblemChanges(actualLogFile.getPath());
		assertEquals(expectedProblems, actualProblems);
	}

	public void clean() throws CoreException {
		actualLogFile.delete();
	}

	public void generateExpectedLog() throws CoreException {
		if (actualLogFile.exists() && !expectedLogFile.exists()) {
			actualLogFile.copyTo(expectedLogFile);
		}
	}

}
