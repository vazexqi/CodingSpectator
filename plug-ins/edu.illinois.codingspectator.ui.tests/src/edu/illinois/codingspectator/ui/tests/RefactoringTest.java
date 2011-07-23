/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ltk.core.refactoring.codingspectator.RunningModes;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCorePlugin;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.illinois.codingspectator.data.CodingSpectatorDataPlugin;
import edu.illinois.codingspectator.efs.EFSFile;
import edu.illinois.codingspectator.refactoringproblems.logger.ProblemChanges;
import edu.illinois.codingspectator.refactorings.parser.RefactoringLog;

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
@SuppressWarnings("restriction")
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
			EFSFile file= new EFSFile(getPathToExpectedResultsOfTest());

			if (file.exists()) {
				HashSet<String> expectedLogs= new HashSet<String>();
				expectedLogs.addAll(file.childNames());

				if (expectedLogs.contains(ProblemChanges.REFACTORING_PROBLEMS_LOG)) {
					logCheckers.add(new RefactoringProblemsChecker(getExpectedRefactoringProblemsLogPath()));
				}
				addRefactoringLogCheckers(expectedLogs);
			} else {
				printMessage(String.format("Expected logs for %s are missing.", getTestName()));
			}
		}

		return logCheckers;
	}

	private IPath getExpectedRefactoringProblemsLogPath() {
		return getPathToExpectedResultsOfTest().append(ProblemChanges.REFACTORING_PROBLEMS_LOG);
	}

	private void addRefactoringLogCheckers(Set<String> expectedHistoryFolderNames) {
		for (String expectedHistoryFolderName : expectedHistoryFolderNames) {
			if (RefactoringLog.isLogType(expectedHistoryFolderName)) {
				logCheckers.add(new RefactoringLogChecker(RefactoringLog.toLogType(expectedHistoryFolderName), getRefactoringKind(), getTestName(), getProjectName()));
			}
		}
	}

	protected Collection<LogChecker> getAllLogCheckers() {
		Collection<LogChecker> allLogCheckers= new ArrayList<LogChecker>();
		for (RefactoringLog.LogType logType : RefactoringLog.getLogTypes()) {
			allLogCheckers.add(new RefactoringLogChecker(logType, getRefactoringKind(), getTestName(), getProjectName()));
		}
		allLogCheckers.add(new RefactoringProblemsChecker(getExpectedRefactoringProblemsLogPath()));
		return allLogCheckers;
	}

	private IPath getPathToExpectedResultsOfTest() {
		return new Path(RefactoringLogUtils.EXPECTED_DESCRIPTORS).append(getRefactoringKind()).append(getTestName());
	}

	protected void doAddJavaClass() throws Exception {
		bot.createANewJavaClass(getProjectName(), getTestFileName());
		bot.prepareJavaTextInEditor(getRefactoringKind(), getTestFileFullName());
	}

	protected void waitUntilLogsAreEmpty() throws CoreException {
		bot.waitUntil(new DefaultCondition() {

			@Override
			public boolean test() throws Exception {
				boolean allActualLogsAreEmpty= true;
				for (LogChecker logChecker : getLogCheckers()) {
					allActualLogsAreEmpty&= !logChecker.actualLogExists();
				}
				return allActualLogsAreEmpty;
			}

			@Override
			public String getFailureMessage() {
				return "Some of the actual logs still exist.";
			}
		});
	}

	protected void doLogsShouldBeEmpty() throws CoreException {
		waitUntilLogsAreEmpty();
		for (LogChecker logChecker : getLogCheckers()) {
			logChecker.assertActualLogIsEmpty();
		}
	}

	abstract protected void doExecuteRefactoring() throws Exception;

	protected void printMessage(String message) {
		System.err.println(getClass() + ": " + message);
	}

	protected void doGenerateExpectedFiles() throws Exception {
		if (RunningModes.shouldGenerateExpectedFiles() || RunningModes.shouldOverwriteExpectedFiles()) {
			for (LogChecker logChecker : getAllLogCheckers()) {
				logChecker.generateExpectedLog(RunningModes.shouldOverwriteExpectedFiles());
			}
		}
	}

	protected void doLogsShouldBeCorrect() throws Exception {
		for (LogChecker logChecker : getLogCheckers()) {
			logChecker.assertMatch();
		}
	}

	protected void doCleanLogs() throws CoreException {
		new EFSFile(CodingSpectatorDataPlugin.getStorageLocation()).delete();
		new EFSFile(RefactoringCorePlugin.getDefault().getStateLocation()).delete();
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
		doAddJavaClass();
	}

	@Test
	public final void logsShouldBeEmpty() throws CoreException {
		doLogsShouldBeEmpty();
	}

	@Test
	public final void shouldExecuteRefactoring() throws Exception {
		doExecuteRefactoring();
	}

	@Test
	public final void logsShouldBeCorrect() throws Exception {
		bot.sleep();
		doGenerateExpectedFiles();
		bot.sleep();
		doLogsShouldBeCorrect();
	}

	@Test
	public final void shouldCleanUpWorkspace() throws CoreException {
		bot.deleteProject(getProjectName());
		bot.sleep();
		doCleanLogs();
		doLogsShouldBeEmpty();
		bot.sleep();
	}

}
