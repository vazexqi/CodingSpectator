/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.illinois.codingspectator.refactoringproblems.logger.ProblemChanges;

/**
 * The methods marked with @Test annotation are final because if you override them, the order in
 * which SWTBot executes the test methods changes. To change the behavior of a SWTBot test method,
 * override the template method called in that SWTBot test method. The names of these template
 * methods begin with "do".
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * @author Balaji Ambresh Rajkumar
 * 
 */
public abstract class RefactoringTest {

	protected static CodingSpectatorBot bot;

	private Collection<LogChecker> logCheckers;

	protected String getProjectName() {
		return "Prj";
	}

	protected abstract String getTestFileName();

	protected String getTestFileFullName() {
		return getTestFileName() + ".java";
	}

	protected String getRefactoringKind() {
		String packageFullyQualifiedName= getClass().getPackage().getName();
		String[] subpackages= packageFullyQualifiedName.split("\\.");
		return subpackages[subpackages.length - 1];
	}

	protected String getTestName() {
		return getClass().getSimpleName();
	}

	protected Collection<LogChecker> getLogCheckers() throws CoreException {
		if (logCheckers == null) {
			logCheckers= new ArrayList<LogChecker>();
			IFileStore fileStore= EFS.getLocalFileSystem().getStore(getPathToExpectedResultsOfTest());

			if (fileStore.fetchInfo().exists()) {
				HashSet<String> expectedLogs= new HashSet<String>();
				expectedLogs.addAll(Arrays.asList(fileStore.childNames(EFS.NONE, null)));

				if (expectedLogs.contains(ProblemChanges.REFACTORING_PROBLEMS_LOG)) {
					logCheckers.add(new RefactoringProblemsChecker(getRefactoringProblemsLogPath()));
				}
				addRefactoringLogCheckers(expectedLogs);
			} else {
				printMessage(String.format("Expected descriptors for %s are missing.", getTestName()));
			}
		}

		return logCheckers;
	}

	private IPath getRefactoringProblemsLogPath() {
		return getPathToExpectedResultsOfTest().append(ProblemChanges.REFACTORING_PROBLEMS_LOG);
	}
	
	private IPath getActualRefactoringProblemsLogPath() {
		String actualPath = RefactoringLog.getRefactoringStorageLocation("refactorings/").toOSString() + ProblemChanges.REFACTORING_PROBLEMS_LOG;
		return Path.fromOSString(actualPath);
	}

	private void addRefactoringLogCheckers(Set<String> expectedHistoryFolderNames) {
		for (String expectedHistoryFolderName : expectedHistoryFolderNames) {
			if (RefactoringLog.isLogType(expectedHistoryFolderName)) {
				logCheckers.add(new RefactoringLogChecker(RefactoringLog.toLogType(expectedHistoryFolderName), getRefactoringKind(), getTestName(), getProjectName()));
			}
		}
	}

	private IPath getPathToExpectedResultsOfTest() {
		return new Path(RefactoringLogUtils.EXPECTED_DESCRIPTORS).append(getRefactoringKind()).append(getTestName());
	}

	protected void doRefactoringLogShouldBeEmpty() throws CoreException {
		for (LogChecker logChecker : getLogCheckers()) {
			logChecker.assertLogIsEmpty();
		}
	}

	protected void doExecuteRefactoring() throws CoreException {
	}

	protected void printMessage(String message) {
		System.err.println(getClass() + ": " + message);
	}

	protected void doRefactoringShouldBeLogged() throws CoreException {
		for (LogChecker logChecker : getLogCheckers()) {
			logChecker.assertMatch();
		}
	}

	protected void doCleanRefactoringHistory() throws CoreException {
		for (LogChecker logChecker : getLogCheckers()) {
			logChecker.clean();
		}
	}
	
	private void cleanUpWorkspace() throws CoreException {
		bot.deleteProject(getProjectName());
		EFS.getLocalFileSystem().getStore(getActualRefactoringProblemsLogPath()).delete(EFS.NONE, null);
		bot.sleep();
		// Deleting a project is a refactoring that gets logged in .refactorings/.workspace.
		// We need to delete the .refactoring folder after the deletion of the project,
		// otherwise, the next test fails because it expects the refactoring history folder to be empty initially.
		bot.deleteEclipseRefactoringLog();
		bot.sleep();
	}

	// SWTBot tests run in the order in which they are declared.

	@BeforeClass
	public static void beforeClass() throws Exception {
		bot= new CodingSpectatorBot();
		bot.dismissWelcomeScreenIfPresent();
	}

	@Test
	public final void setupProject() throws Exception {
		bot.createANewJavaProject(getProjectName());
		bot.createANewJavaClass(getProjectName(), getTestFileName());
		bot.prepareJavaTextInEditor(getRefactoringKind(), getTestFileFullName());
	}

	//FIXME: Rename this method to logsShouldBeEmpty
	@Test
	public final void refactoringLogShouldBeEmpty() throws CoreException {
		bot.sleep();
		doRefactoringLogShouldBeEmpty();
	}

	@Test
	public final void shouldExecuteRefactoring() throws CoreException {
		doExecuteRefactoring();
	}

	@Test
	public final void refactoringShouldBeLogged() throws CoreException {
		bot.sleep();
		doRefactoringShouldBeLogged();
	}

	@Test
	public final void cleanRefactoringHistory() throws CoreException {
		doCleanRefactoringHistory();
	}

	@Test
	public final void deleteCurrentProject() throws CoreException {
		cleanUpWorkspace();
		doRefactoringLogShouldBeEmpty();
		bot.sleep();
	}
}
