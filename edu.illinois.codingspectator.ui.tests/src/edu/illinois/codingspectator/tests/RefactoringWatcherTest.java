package edu.illinois.codingspectator.tests;


import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Path;
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
import org.osgi.framework.Version;


/**
 * Superclass to encapsulate common functionalities for testing refactorings.
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public abstract class RefactoringWatcherTest {

	private static final String GENERIC_VERSION_NUMBER= "1.1.1.qualifier";

	static final String PLUGIN_NAME= "edu.illinois.codingspectator.ui.tests";

	static final String REFACTORING_HISTORY_LOCATION= Platform.getStateLocation(Platform.getBundle("org.eclipse.ltk.core.refactoring")).toOSString();

	static final String CANCELED_REFACTORINGS= "refactorings/canceled";

	static final String PERFORMED_REFACTORINGS= "refactorings/performed";

	static final String PACKAGE_NAME= "edu.illinois.codingspectator";

	static SWTWorkbenchBot bot;

	protected IFileStore performedRefactorings;

	protected IFileStore canceledRefactorings;

	private static Version getFeatureVersion() {
		Bundle bundle= Platform.getBundle("edu.illinois.codingspectator.monitor");
		if (bundle != null)
			return bundle.getVersion();
		else
			return new Version(GENERIC_VERSION_NUMBER);
	}

	{
		performedRefactorings= EFS.getLocalFileSystem().getStore(new Path(getRefactoringStorageLocation(PERFORMED_REFACTORINGS)));
		canceledRefactorings= EFS.getLocalFileSystem().getStore(new Path(getRefactoringStorageLocation(CANCELED_REFACTORINGS)));
	}

	private String getRefactoringStorageLocation(String directory) {

		String fullDirectory= REFACTORING_HISTORY_LOCATION + getSystemFileSeparator() + getFeatureVersion();

		String directorySeparator= "/";
		String[] directories= directory.split(directorySeparator);
		for (int i= 0; i < directories.length; i++)
			fullDirectory+= getSystemFileSeparator() + directories[i];

		return fullDirectory;
	}

	private String getSystemFileSeparator() {
		return System.getProperty("file.separator");
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

	abstract public void prepareRefactoring();

	abstract String getProjectName();

	abstract String getTestFileName();



}
