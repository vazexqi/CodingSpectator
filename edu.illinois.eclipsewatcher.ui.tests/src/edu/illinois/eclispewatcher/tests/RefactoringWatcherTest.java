package edu.illinois.eclispewatcher.tests;


import java.io.File;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import edu.illinois.eclipsewatcher.test.utils.FileUtilities;


/**
 * Superclass to encapsulate common functionalities for testing refactorings.
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public abstract class RefactoringWatcherTest {
	
	static final String PLUGIN_NAME= "edu.illinois.eclipsewatcher.ui.tests";

	static final String REFACTORING_HISTORY_LOCATION= Platform.getStateLocation(Platform.getBundle("org.eclipse.ltk.core.refactoring")).toOSString();

	static final String CANCELED_REFACTORINGS= ".refactorings.canceled";

	static final String PERFORMED_REFACTORINGS= ".refactorings.performed";

	static final String PACKAGE_NAME= "edu.illinois.eclipsewatcher";

	static SWTWorkbenchBot bot;

	static File performedRefactorings;

	static File canceledRefactorings;

	static {
		performedRefactorings= new File(REFACTORING_HISTORY_LOCATION + System.getProperty("file.separator") + PERFORMED_REFACTORINGS);
		canceledRefactorings= new File(REFACTORING_HISTORY_LOCATION + System.getProperty("file.separator") + CANCELED_REFACTORINGS);
	}

	@BeforeClass
	public static void beforeClass() throws Exception {
		bot= new SWTWorkbenchBot();
		bot.viewByTitle("Welcome").close();
	}

	public void canCreateANewJavaProject() throws Exception {
		bot.menu("File").menu("New").menu("Project...").click();

		bot.shell("New Project").activate();
		bot.tree().expandNode("Java").select("Java Project");
		bot.button("Next >").click();

		bot.textWithLabel("Project name:").setText(getProjectName());

		bot.button("Finish").click();

		bot.button("Yes").click();
	}


	public void canCreateANewJavaClass() throws Exception {
		bot.menu("File").menu("New").menu("Class").click();

		bot.shell("New Java Class").activate();
//		bot.textWithLabel("Source folder:").setText(getProjectName() + "/src");

		bot.textWithLabel("Package:").setText(PACKAGE_NAME);
		bot.textWithLabel("Name:").setText(getTestFileName());

		bot.button("Finish").click();

	}

	@AfterClass
	public static void cleanRefactoringHistory() {
		FileUtilities.cleanDirectory(performedRefactorings);
		FileUtilities.cleanDirectory(canceledRefactorings);
	}

	abstract protected void prepareRefactoring();

	abstract String getProjectName();

	abstract String getTestFileName();


}
