package edu.illinois.refactorbehavior;

import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEclipseEditor;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.FileUtils;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;

@RunWith(SWTBotJunit4ClassRunner.class)
public class SWTBotTest {

	private static final String PACKAGE_NAME= "edu.illinois.refactorbehavior";

	private static final String TEST_NAME= "Test0";

	private static SWTWorkbenchBot bot;

	final static String PROJECT_NAME= "MyFirstProject";

	@BeforeClass
	public static void beforeClass() throws Exception {
		bot= new SWTWorkbenchBot();
		SWTBotPreferences.PLAYBACK_DELAY= 10;
		bot.viewByTitle("Welcome").close();
	}

	@Test
	public void canCreateANewJavaProject() throws Exception {
		bot.menu("File").menu("New").menu("Project...").click();

		bot.shell("New Project").activate();
		bot.tree().expandNode("Java").select("Java Project");
		bot.button("Next >").click();

		bot.textWithLabel("Project name:").setText(PROJECT_NAME);

		bot.button("Finish").click();

		bot.button("Yes").click();
		// FIXME: assert that the project is actually created, for later
	}

	@Test
	public void canCreateANewJavaClass() throws Exception {
		// bot.toolbarDropDownButtonWithTooltip("New Java Class").menuItem("Class").click();
		bot.menu("File").menu("New").menu("Class").click();

		bot.shell("New Java Class").activate();
		bot.textWithLabel("Source folder:").setText("MyFirstProject/src");

		bot.textWithLabel("Package:").setText(PACKAGE_NAME);
		bot.textWithLabel("Name:").setText(TEST_NAME);

		bot.button("Finish").click();

		// FIXME: assert that the class is actually created, for later
	}

	@Test
	public void canTypeInTextInAJavaClass() throws Exception {

		Bundle bundle= Platform.getBundle("edu.illinois.refactorbehavior.tests");
		String contents= FileUtils.read(bundle.getEntry("test-files/" + TEST_NAME + ".java"));

		SWTBotEclipseEditor editor= bot.editorByTitle(TEST_NAME + ".java")
				.toTextEditor();
		editor.setText(contents);
		editor.save();

		// FIXME: verify that the text is actually placed in the editor
	}

	@Test
	public void shouldExtractMethod() throws Exception {
		SWTBotEclipseEditor editor= bot.editorByTitle(TEST_NAME + ".java").toTextEditor();
		editor.setFocus();
		// editor.selectLine(5);
		editor.selectRange(5, 2, 37 - 9);
		System.out.println(editor.getSelection());
		System.out.println(editor.cursorPosition());
		SWTBotMenu refactorMenu= bot.menu("Refactor");
		assertTrue(refactorMenu.isEnabled());
		SWTBotMenu extractMethodMenuItem= refactorMenu.menu("Extract Method...");
		assertTrue(extractMethodMenuItem.isEnabled());
		extractMethodMenuItem.click();
		// editor.pressShortcut(Keystrokes.SHIFT, Keystrokes.ALT,
		// KeyStroke.getInstance("M"));
		bot.shell("Extract Method").activate();
		bot.textWithLabel("Method name:").setText("m");
		bot.button("OK").click();
		bot.sleep(200000);
		// FIXME: verify that the text is actually placed in the editor
	}

	@AfterClass
	public static void sleep() {
		bot.sleep(2000);
	}

}
