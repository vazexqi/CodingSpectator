/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import java.util.List;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import edu.illinois.codingspectator.refactoringproblems.logger.ProblemChanges;
import edu.illinois.codingspectator.refactoringproblems.parser.RefactoringProblemsLogDeserializer;
import edu.illinois.codingspectator.refactoringproblems.parser.RefactoringProblemsParserException;

/**
 * @author Mohsen Vakilian
 * @author Balaji Ambresh Rajkumar
 * 
 */
public class RefactoringProblemsChecker implements LogChecker {

	private final IPath logPath;
	private final String actualPath = RefactoringLog.getRefactoringStorageLocation("refactorings/").toOSString() + ProblemChanges.REFACTORING_PROBLEMS_LOG;
	private final IFileStore fileStore= EFS.getLocalFileSystem().getStore(Path.fromOSString(actualPath));
	
	public RefactoringProblemsChecker(IPath logPath) {
		this.logPath= logPath;
	}

	@Override
	public void assertLogIsEmpty() {
		assertFalse(fileStore.fetchInfo().exists());
	}

	@Override
	public void assertMatch() {
		try {
			List<ProblemChanges> expectedProblems = new RefactoringProblemsLogDeserializer(false).deserializeRefactoringProblemsLog(logPath.toOSString());
			List<ProblemChanges> actualProblems = new RefactoringProblemsLogDeserializer(false).deserializeRefactoringProblemsLog(actualPath);		
			assertEquals(expectedProblems, actualProblems);
		} catch (RefactoringProblemsParserException e) {
			//FIXME: Log exceptions
			e.printStackTrace();
			fail("the problems log descriptors do not match");
		}
	}

	@Override
	public void clean() throws CoreException {
		fileStore.delete(EFS.NONE, null);
	}

}
