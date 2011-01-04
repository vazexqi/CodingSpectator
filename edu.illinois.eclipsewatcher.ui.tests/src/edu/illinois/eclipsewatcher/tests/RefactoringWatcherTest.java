package edu.illinois.eclipsewatcher.tests;


import java.io.File;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.swt.finder.utils.FileUtils;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.junit.BeforeClass;
import org.osgi.framework.Bundle;


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

	protected File performedRefactorings;

	protected File canceledRefactorings;

	{
		performedRefactorings= new File(REFACTORING_HISTORY_LOCATION + System.getProperty("file.separator") + PERFORMED_REFACTORINGS);
		canceledRefactorings= new File(REFACTORING_HISTORY_LOCATION + System.getProperty("file.separator") + CANCELED_REFACTORINGS);
	}

	@BeforeClass
	public static void beforeClass() throws Exception {
		bot= new SWTWorkbenchBot();
		dismissWelcomeScreenIfPresent();
	}

	private static void dismissWelcomeScreenIfPresent() {
		try {
			bot.viewByTitle("Welcome").close();
		} catch (WidgetNotFoundException exception) {
			// The welcome screen might not be shown so just ignore
		}
	}

	public void canCreateANewJavaProject() throws Exception {
		bot.menu("File").menu("New").menu("Project...").click();

		bot.shell("New Project").activate();
		bot.tree().expandNode("Java").select("Java Project");
		bot.button("Next >").click();

		bot.textWithLabel("Project name:").setText(getProjectName());

		bot.button("Finish").click();

		dismissJavaPerspectiveIfPresent();
	}

	private void dismissJavaPerspectiveIfPresent() {
		try {
			bot.button("Yes").click();
		} catch (WidgetNotFoundException exception) {
			// The second and subsequent time this is invoked the Java perspective change dialog will not be shown.
		}
	}


	public void canCreateANewJavaClass() throws Exception {
		selectCurrentJavaProject();

		bot.menu("File").menu("New").menu("Class").click();

		bot.shell("New Java Class").activate();

		bot.textWithLabel("Package:").setText(PACKAGE_NAME);
		bot.textWithLabel("Name:").setText(getTestFileName());

		bot.button("Finish").click();
	}

	private void selectCurrentJavaProject() {
		SWTBotView packageExplorerView= bot.viewByTitle("Package Explorer");
		packageExplorerView.show();

		Composite packageExplorerComposite= (Composite)packageExplorerView.getWidget();

		Tree swtTree= (Tree)bot.widget(WidgetMatcherFactory.widgetOfType(Tree.class), packageExplorerComposite);
		SWTBotTree tree= new SWTBotTree(swtTree);

		tree.select(getProjectName());
	}

	public void prepareJavaTextInEditor() throws Exception {

		Bundle bundle= Platform.getBundle(PLUGIN_NAME);
		String contents= FileUtils.read(bundle.getEntry("test-files/" + getTestFileName() + ".java"));

		SWTBotEclipseEditor editor= bot.editorByTitle(getTestFileName() + ".java").toTextEditor();
		editor.setText(contents);
		editor.save();
	}

	abstract protected void prepareRefactoring();

	abstract String getProjectName();

	abstract String getTestFileName();



}
