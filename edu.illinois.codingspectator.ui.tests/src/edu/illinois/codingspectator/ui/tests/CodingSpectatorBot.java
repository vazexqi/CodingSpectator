/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests;

import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.swt.finder.utils.FileUtils;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.osgi.framework.Bundle;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class CodingSpectatorBot {

	public static final String CONTINUE_LABEL= "Continue";

	static final String PLUGIN_NAME= "edu.illinois.codingspectator.ui.tests";

	static final String PACKAGE_NAME= "edu.illinois.codingspectator";

	protected static final String REFACTOR_MENU_NAME= "Refactor";

	protected static final int SLEEPTIME= 1500;

	SWTWorkbenchBot bot= new SWTWorkbenchBot();

	public void dismissWelcomeScreenIfPresent() {
		try {
			bot.viewByTitle("Welcome").close();
		} catch (WidgetNotFoundException exception) {
			// The welcome screen might not be shown so just ignore
		}
	}

	public void createANewJavaProject(String projectName) throws Exception {
		bot.menu("File").menu("New").menu("Project...").click();

		bot.shell("New Project").activate();
		bot.tree().expandNode("Java").select("Java Project");
		bot.button(IDialogConstants.NEXT_LABEL).click();

		bot.textWithLabel("Project name:").setText(projectName);

		bot.button(IDialogConstants.FINISH_LABEL).click();

		dismissJavaPerspectiveIfPresent();
	}

	private void dismissJavaPerspectiveIfPresent() {
		try {
			bot.button(IDialogConstants.YES_LABEL).click();
		} catch (WidgetNotFoundException exception) {
			// The second and subsequent time this is invoked the Java perspective change dialog will not be shown.
		}
	}

	public void createANewJavaClass(String projectName, String testFileName) throws Exception {
		selectJavaProject(projectName);

		bot.menu("File").menu("New").menu("Class").click();

		bot.shell("New Java Class").activate();

		bot.textWithLabel("Package:").setText(PACKAGE_NAME);
		bot.textWithLabel("Name:").setText(testFileName);

		bot.button(IDialogConstants.FINISH_LABEL).click();
	}

	public SWTBotTree selectJavaProject(String projectName) {
		SWTBotView packageExplorerView= bot.viewByTitle("Package Explorer");
		packageExplorerView.show();

		Composite packageExplorerComposite= (Composite)packageExplorerView.getWidget();

		Tree swtTree= (Tree)bot.widget(WidgetMatcherFactory.widgetOfType(Tree.class), packageExplorerComposite);
		SWTBotTree tree= new SWTBotTree(swtTree);

		return tree.select(projectName);
	}

	public void prepareJavaTextInEditor(String testInputLocation, String testFileFullName) throws Exception {
		Bundle bundle= Platform.getBundle(PLUGIN_NAME);
		String contents= FileUtils.read(bundle.getEntry("test-files-new/" + testInputLocation + "/" + testFileFullName));

		SWTBotEclipseEditor editor= bot.editorByTitle(testFileFullName).toTextEditor();
		editor.setText(contents);
		editor.save();
	}

	public void closeProject(String projectName) {
		selectJavaProject(projectName).contextMenu("Close Project").click();
	}

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
}
