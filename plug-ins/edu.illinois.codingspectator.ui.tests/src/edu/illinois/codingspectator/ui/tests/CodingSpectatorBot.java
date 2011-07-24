/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.allOf;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.widgetOfType;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withMnemonic;
import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withStyle;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCorePlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.waits.Conditions;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.utils.FileUtils;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.hamcrest.Matcher;
import org.osgi.framework.Bundle;

import edu.illinois.codingspectator.efs.EFSFile;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * @author Balaji Ambresh Rajkumar
 * 
 */
@SuppressWarnings("restriction")
public class CodingSpectatorBot {

	public static final String CONTINUE_LABEL= "Continue";

	public static final String PREVIEW_LABEL= "Preview >";

	static final String PLUGIN_NAME= "edu.illinois.codingspectator.ui.tests";

	public static final String PACKAGE_NAME= "edu.illinois.codingspectator";

	protected static final String REFACTOR_MENU_NAME= "Refactor";

	protected static final int SLEEPTIME= 500;

	SWTWorkbenchBot bot= new SWTWorkbenchBot();

	public void dismissWelcomeScreenIfPresent() {
		try {
			bot.viewByTitle("Welcome").close();
		} catch (WidgetNotFoundException exception) {
			// The welcome screen might not be shown so just ignore
		}
	}

	public SWTWorkbenchBot getBot() {
		return bot;
	}

	public void waitUntil(ICondition condition) {
		bot.waitUntil(condition);
	}

	public void createANewJavaProject(String projectName) {
		bot.menu("File").menu("New").menu("Project...").click();

		final SWTBotShell shell= activateShellWithName("New Project");

		getCurrentTree().expandNode("Java").select("Java Project");
		bot.button(IDialogConstants.NEXT_LABEL).click();

		bot.textWithLabel("Project name:").setText(projectName);

		bot.button(IDialogConstants.FINISH_LABEL).click();

		waitUntil(new DefaultCondition() {

			@Override
			public boolean test() throws Exception {
				return Conditions.shellCloses(shell).test() || bot.shell("Open Associated Perspective?").isVisible();
			}

			@Override
			public String getFailureMessage() {
				return "Failed to close the new project wizard.";
			}
		});
		dismissJavaPerspectiveIfPresent();
	}

	private EFSFile getEclipseRefactoringsEFSFile() {
		return new EFSFile(RefactoringCorePlugin.getDefault().getStateLocation().append(".refactorings"));
	}

	private void deleteProjectSpecificEclipseRefactoringLog(String projectName) throws CoreException {
		getEclipseRefactoringsEFSFile().append(projectName).delete();
	}

	public void deleteEclipseRefactoringLog() throws CoreException {
		getEclipseRefactoringsEFSFile().delete();
	}

	public void deleteProject(String projectName) throws CoreException {
		selectJavaProject(projectName).contextMenu("Delete").click();
		SWTBotShell shell= activateShellWithName("Delete Resources");
		if (!bot.checkBox().isChecked()) {
			bot.checkBox().click();
		}
		bot.button(IDialogConstants.OK_LABEL).click();
		deleteProjectSpecificEclipseRefactoringLog(projectName);
		waitUntil(Conditions.shellCloses(shell));
	}

	public SWTBotTree getCurrentTree() {
		return bot.tree();
	}

	public SWTBotShell activateShellWithName(String text) {
		SWTBotShell shell= bot.shell(text);
		shell.activate();
		return shell;
	}

	private void dismissJavaPerspectiveIfPresent() {
		try {
			bot.button(IDialogConstants.YES_LABEL).click();
		} catch (WidgetNotFoundException exception) {
			// The second and subsequent time this is invoked the Java perspective change dialog will not be shown.
		}
	}

	public void createANewJavaClass(String projectName, final String className) {
		selectJavaProject(projectName);

		bot.menu("File").menu("New").menu("Class").click();

		activateShellWithName("New Java Class");

		bot.textWithLabel("Package:").setText(PACKAGE_NAME);
		bot.textWithLabel("Name:").setText(className);

		bot.button(IDialogConstants.FINISH_LABEL).click();

		waitUntil(new DefaultCondition() {

			@Override
			public boolean test() throws Exception {
				return getTextEditor(className + ".java") != null;
			}

			@Override
			public String getFailureMessage() {
				return "Failed to find the open editor of class " + className;
			}
		});
	}

	public SWTBotTree selectJavaProject(String projectName) {
		SWTBotView packageExplorerView= bot.viewByTitle("Package Explorer");
		packageExplorerView.show();

		Composite packageExplorerComposite= (Composite)packageExplorerView.getWidget();

		Tree swtTree= (Tree)bot.widget(widgetOfType(Tree.class), packageExplorerComposite);
		SWTBotTree tree= new SWTBotTree(swtTree);

		return tree.select(projectName);
	}

	public SWTBotEclipseEditor getTextEditor(String editorTitle) {
		return bot.editorByTitle(editorTitle).toTextEditor();
	}

	public void prepareJavaTextInEditor(String testInputLocation, String testFileFullName) throws Exception {
		Bundle bundle= Platform.getBundle(PLUGIN_NAME);
		String contents= FileUtils.read(bundle.getEntry("test-files/" + testInputLocation + "/" + testFileFullName));

		SWTBotEclipseEditor editor= getTextEditor(testFileFullName);
		editor.setText(contents);
		editor.save();
	}

	/**
	 * Prefer {@link #waitUntil(ICondition)} over {@link #sleep()} in your tests because it's faster
	 * and makes the tests more reliable.
	 */
	public void sleep() {
		bot.sleep(SLEEPTIME);
	}

	public void invokeRefactoringFromMenu(String refactoringMenuItemName) {
		SWTBotMenu refactorMenu= bot.menu(REFACTOR_MENU_NAME);
		assertTrue(refactorMenu.isEnabled());

		SWTBotMenu refactoringMenuItem= refactorMenu.menu(refactoringMenuItemName);
		assertTrue(refactoringMenuItem.isEnabled());

		refactoringMenuItem.click();
	}

	public void clickButtons(String... buttonNames) {
		for (String buttonName : buttonNames) {
			bot.button(buttonName).click();
		}
	}

	/**
	 * Selects part of the given line from the given column number for the specified length.
	 * 
	 * @param line The line number to be selected. This number is zero-based.
	 * @param column The column number of the beginning of the selection. This number is zero-based.
	 *            If you use Eclipse editor to find the column number, you should be aware that the
	 *            Eclipse editor displays offsets of the caret by expanding tabs into spaces. So, to
	 *            get the column number from Eclipse editor safely, convert the tabs to spaces
	 *            first.
	 * @param length The length of the selection.
	 */
	public void selectElementToRefactor(String testFileFullName, int line, int column, int length) {
		SWTBotEclipseEditor editor= bot.editorByTitle(testFileFullName).toTextEditor();

		editor.setFocus();
		editor.selectRange(line, column, length);
	}

	public void fillTextField(String textFieldLabel, String textFieldValue) {
		bot.textWithLabel(textFieldLabel).setText(textFieldValue);
	}

	public void setComboBox(String label, String value) {
		bot.comboBoxWithLabel(label).setText(value);
	}

	/**
	 * Selects the element at the given path from the package explorer view.
	 */
	public void selectFromPackageExplorer(String projectName, String... pathElements) {
		SWTBotTree tree= selectJavaProject(projectName);
		SWTBotTreeItem treeItem= tree.getTreeItem(projectName).expand();

		for (int i= 0; i < pathElements.length - 1; i++) {
			treeItem= treeItem.expandNode(pathElements[i]);
		}

		if (pathElements.length > 0) {
			treeItem.select(pathElements[pathElements.length - 1]);
		}
	}

	/**
	 * 
	 * This method provides a workaround for Eclipse bug 344484.
	 * 
	 * @param radioText
	 */
	public void deselectRadio(final String radioText) {
		UIThreadRunnable.syncExec(new VoidResult() {

			public void run() {
				@SuppressWarnings("unchecked")
				Matcher<Widget> matcher= allOf(widgetOfType(Button.class), withStyle(SWT.RADIO, "SWT.RADIO"), withMnemonic(radioText));

				Button b= (Button)bot.widget(matcher);
				b.setSelection(false);
			}

		});
	}

}
