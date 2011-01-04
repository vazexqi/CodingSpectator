/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.tests;

import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.runner.RunWith;

/**
 * This test will currently FAIL
 * 
 * @author Mohsen Vakilian
 * @author nchen
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class ExtractMethodTest extends RefactoringWatcherTest {

	@Override
	public void prepareRefactoring() {
		// TODO Auto-generated method stub

	}

	@Override
	String getProjectName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	String getTestFileName() {
		// TODO Auto-generated method stub
		return null;
	}
//	static final String TEST_FILE_NAME= "ExtractMethodTestFile";
//
//	static final String PROJECT_NAME= "MyFirstProject_" + ExtractMethodTest.class;
//
//	@Test
//	public void canSetupProject() throws Exception {
//		super.canCreateANewJavaProject();
//		super.canCreateANewJavaClass();
//		super.prepareJavaTextInEditor();
//	}
//
//	///////////////////////////////////////////////////////////////////////////
//	//
//	// SWTBot tests run in order which is why we can take advantage of this
//	// and capture the canceled refactoring first and then do the actual 
//	// refactoring. Currently we are just testing that the appropriates folders 
//	// are created.
//	//
//	///////////////////////////////////////////////////////////////////////////
//
//
//	@Test
//	public void currentRefactoringsCapturedShouldBeEmpty() {
//		assertFalse(performedRefactorings.exists());
//		assertFalse(canceledRefactorings.exists());
//	}
//
//	@Test
//	public void shouldCaptureCancelledRefactoring() {
//		prepareRefactoring();
//
//		bot.shell("Extract Method").activate();
//		bot.button("Cancel").click();
//	}
//
//	@Test
//	// This needs to be interleaved here after the refactoring has been canceled.
//	public void currentRefactoringsCanceledShouldBePopulated() {
//		assertFalse(performedRefactorings.exists());
//		assertTrue(canceledRefactorings.exists());
//	}
//
//	@Test
//	public void shouldCapturePerformedRefactoring() throws Exception {
//		prepareRefactoring();
//
//		bot.shell("Extract Method").activate();
//		bot.button("OK").click();
//	}
//
//	@Test
//	// This needs to be interleaved here after the refactoring has been performed.
//	public void currentRefactoringsPerformedShouldBePopulated() {
//		bot.sleep(2000);
//		assertTrue(canceledRefactorings.exists());
//		assertTrue(performedRefactorings.exists());
//	}
//
//	// This is a hack to ensure that refactorings are cleared at the end of each test
//	@Test
//	public void cleanRefactoringHistory() {
//		bot.sleep(2000);
//		FileUtilities.cleanDirectory(performedRefactorings);
//		FileUtilities.cleanDirectory(canceledRefactorings);
//	}
//
//	@Override
//	protected void prepareRefactoring() {
//		final SWTBotEclipseEditor editor= bot.editorByTitle(TEST_FILE_NAME + ".java").toTextEditor();
//
//		// Extract Method Refactoring
//		editor.setFocus();
//
//		final int cursorLinePosition= 5;
//		final int cursorColumnBeginPosition= 8;
//		int cursorColumnEndPosition= 36;
//		editor.selectRange(cursorLinePosition, cursorColumnBeginPosition, cursorColumnEndPosition - cursorColumnBeginPosition);
//
//		DefaultCondition condition= new DefaultCondition() {
//
//			@Override
//			public boolean test() throws Exception {
//				return editor.cursorPosition().line == cursorLinePosition;
//			}
//
//			@Override
//			public String getFailureMessage() {
//				return "Cannot update cursor position in editor";
//			}
//
//		};
//
//		bot.waitUntil(condition, 5000, 100);
//		System.out.println("Cursor position:" + editor.cursorPosition());
//
//		SWTBotMenu refactorMenu= bot.menu("Refactor");
//		assertTrue(refactorMenu.isEnabled());
//
//		SWTBotMenu extractMethodMenuItem= refactorMenu.menu("Extract Method...");
//		assertTrue(extractMethodMenuItem.isEnabled());
//
//		extractMethodMenuItem.click();
//	}
//
//	@Override
//	String getProjectName() {
//		return PROJECT_NAME;
//	}
//
//	@Override
//	String getTestFileName() {
//		return TEST_FILE_NAME;
//	}

}
