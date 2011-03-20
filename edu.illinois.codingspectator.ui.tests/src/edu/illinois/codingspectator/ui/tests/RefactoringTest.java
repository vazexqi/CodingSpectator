/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public abstract class RefactoringTest {

	protected static CodingSpectatorBot bot;

	private Collection<RefactoringLogChecker> refactoringLogCheckers= getRefactoringLogCheckers();

	public String getProjectName() {
		return "TestProject_" + getProjectNameSuffix();
	}

	private String getProjectNameSuffix() {
		return getClass().toString();
	}

	protected abstract String getTestFileName();

	protected String getTestFileFullName() {
		return getTestFileName() + ".java";
	}

	protected abstract String getTestInputLocation();

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
		bot.prepareJavaTextInEditor(getTestInputLocation(), getTestFileFullName());
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
	public void closeCurrentProject() {
		bot.closeProject(getProjectName());
	}

}
