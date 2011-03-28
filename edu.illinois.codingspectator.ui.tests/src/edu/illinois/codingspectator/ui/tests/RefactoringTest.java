/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.junit.BeforeClass;
import org.junit.Test;

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

	private Collection<RefactoringLogChecker> refactoringLogCheckers;

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

	protected Collection<RefactoringLogChecker> getRefactoringLogCheckers() throws CoreException {
		if (refactoringLogCheckers == null) {
			refactoringLogCheckers= new ArrayList<RefactoringLogChecker>();
			IFileStore fileStore= EFS.getLocalFileSystem().getStore(new Path(RefactoringLogUtils.EXPECTED_DESCRIPTORS).append(getRefactoringKind()).append(getTestName()));

			if (fileStore.fetchInfo().exists()) {
				String[] expectedHistoryFolderNames= fileStore.childNames(EFS.NONE, null);
				for (String expectedHistoryFolderName : expectedHistoryFolderNames) {
					refactoringLogCheckers.add(new RefactoringLogChecker(RefactoringLog.toLogType(expectedHistoryFolderName), getRefactoringKind(), getTestName(), getProjectName()));
				}
			} else {
				printMessage(String.format("Expected descriptors for %s are missing.", getTestName()));
			}
		}

		return refactoringLogCheckers;
	}

	protected void doRefactoringLogShouldBeEmpty() throws CoreException {
		for (RefactoringLogChecker refactoringLogChecker : getRefactoringLogCheckers()) {
			refactoringLogChecker.assertLogIsEmpty();
		}
	}

	protected void doExecuteRefactoring() throws CoreException {
	}

	protected void printMessage(String message) {
		System.err.println(getClass() + ": " + message);
	}

	protected void doRefactoringShouldBeLogged() throws CoreException {
		for (RefactoringLogChecker refactoringLogChecker : getRefactoringLogCheckers()) {
			refactoringLogChecker.assertMatch();
		}
	}

	protected void doCleanRefactoringHistory() throws CoreException {
		for (RefactoringLogChecker refactoringLogChecker : getRefactoringLogCheckers()) {
			refactoringLogChecker.clean();
		}
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
		bot.deleteProject(getProjectName());
		bot.sleep();
		// Deleting a project is a refactoring that gets logged in .refactorings/.workspace.
		// We need to delete the .refactoring folder after the deletion of the project,
		// otherwise, the next test fails because it expects the refactoring history folder to be empty initially.
		bot.deleteEclipseRefactoringLog();
		bot.sleep();
		doRefactoringLogShouldBeEmpty();
		bot.sleep();
	}
}
