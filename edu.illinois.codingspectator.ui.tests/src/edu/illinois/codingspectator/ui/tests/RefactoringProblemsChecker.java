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

	private final IPath expectedLogPath;

	private final IPath actualLogPath;

	private final EFSFile actuaLogFile;

	public RefactoringProblemsChecker(IPath logPath) {
		this.expectedLogPath= logPath;
		this.actualLogPath= RefactoringLog.getRefactoringStorageLocation("refactorings").append(ProblemChanges.REFACTORING_PROBLEMS_LOG);
		this.actuaLogFile= new EFSFile(actualLogPath);
	}

	@Override
	public void assertLogIsEmpty() {
		assertFalse(actuaLogFile.exists());
	}

	private List<ProblemChanges> getProblemChanges(IPath refactoringLogPath) throws RefactoringProblemsParserException {
		return new RefactoringProblemsLogDeserializer(false).deserializeRefactoringProblemsLog(refactoringLogPath.toOSString());
	}

	@Override
	public void assertMatch() throws RefactoringProblemsParserException {
		List<ProblemChanges> expectedProblems= getProblemChanges(expectedLogPath);
		List<ProblemChanges> actualProblems= getProblemChanges(actualLogPath);
		assertEquals(expectedProblems, actualProblems);
	}

	@Override
	public void clean() throws CoreException {
		actuaLogFile.delete();
	}

}
