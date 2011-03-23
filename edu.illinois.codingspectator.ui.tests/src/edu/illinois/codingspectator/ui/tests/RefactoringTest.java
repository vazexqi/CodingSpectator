/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCorePlugin;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
@SuppressWarnings("restriction")
public abstract class RefactoringTest {

	protected static CodingSpectatorBot bot;

	private Collection<RefactoringLogChecker> refactoringLogCheckers= getRefactoringLogCheckers();

	public String getProjectName() {
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

	protected Collection<RefactoringLogChecker> getRefactoringLogCheckers() {
		return Arrays.asList();
	}

	protected void doRefactoringLogShouldBeEmpty() {
		for (RefactoringLogChecker refactoringLogChecker : refactoringLogCheckers) {
			refactoringLogChecker.assertLogIsEmpty();
		}
	}

	protected void doExecuteRefactoring() {
	}

	protected void doRefactoringShouldBeLogged() {
		for (RefactoringLogChecker refactoringLogChecker : refactoringLogCheckers) {
			refactoringLogChecker.assertMatch();
		}
	}

	protected void doCleanRefactoringHistory() throws CoreException {
		for (RefactoringLogChecker refactoringLogChecker : refactoringLogCheckers) {
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
	public void canSetupProject() throws Exception {
		bot.createANewJavaProject(getProjectName());
		bot.createANewJavaClass(getProjectName(), getTestFileName());
		bot.prepareJavaTextInEditor(getRefactoringKind(), getTestFileFullName());
	}

	@Test
	public void refactoringLogShouldBeEmpty() {
		bot.sleep();
		doRefactoringLogShouldBeEmpty();
	}

	@Test
	public void shouldExecuteRefactoring() {
		doExecuteRefactoring();
	}

	@Test
	public void refactoringShouldBeLogged() throws Exception {
		bot.sleep();
		doRefactoringShouldBeLogged();
	}

	@Test
	public void cleanRefactoringHistory() throws CoreException {
		doCleanRefactoringHistory();
	}

	@Test
	public void deleteCurrentProject() throws CoreException {
		bot.deleteProject(getProjectName());
		bot.sleep();
		// Deleting a project is a refactoring that gets logged in .refactorings/.workspace.
		// We need to delete the .refactoring folder after the deletion of the project,
		// otherwise, the next test fails because it expects the refactoring history folder to be empty initially.
		EFS.getLocalFileSystem().getStore(RefactoringCorePlugin.getDefault().getStateLocation().append(".refactorings")).delete(EFS.NONE, null);
		bot.sleep();
	}


}
