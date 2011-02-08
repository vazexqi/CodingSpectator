/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests;

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

	public String getProjectName() {
		return "TestProject_" + getProjectNameSuffix();
	}

	private String getProjectNameSuffix() {
		return getClass().toString();
	}

	protected abstract String getTestFileName();

	protected abstract String getTestInputLocation();

	protected void doRefactoringLogShouldBeEmpty() {
	}

	protected void doExecuteRefactoring() {
	}

	protected void doRefactoringShouldBeLogged() {
	}

	protected void doCleanRefactoringHistory() throws CoreException {
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
		bot.prepareJavaTextInEditor(getTestInputLocation(), getTestFileName());
	}

	@Test
	public void refactoringLogShouldBeEmpty() {
		bot.sleep();
		doRefactoringLogShouldBeEmpty();
	}

	@Test
	public void shouldExecuteRefac() {
		doExecuteRefactoring();
	}

	@Test
	public void refactoringShouldBeLogged() {
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
