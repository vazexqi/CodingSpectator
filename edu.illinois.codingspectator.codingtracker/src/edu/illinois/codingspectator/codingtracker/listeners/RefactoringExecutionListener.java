/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.listeners;

import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.history.IRefactoringExecutionListener;
import org.eclipse.ltk.core.refactoring.history.RefactoringExecutionEvent;

/**
 * 
 * @author Stas Negara
 * 
 */
public class RefactoringExecutionListener extends BasicListener implements IRefactoringExecutionListener {

	public static void register() {
		RefactoringCore.getHistoryService().addExecutionListener(new RefactoringExecutionListener());
	}

	@Override
	public void executionNotification(RefactoringExecutionEvent event) {
		int eventType= event.getEventType();
		if (eventType == RefactoringExecutionEvent.ABOUT_TO_PERFORM || eventType == RefactoringExecutionEvent.ABOUT_TO_REDO ||
				eventType == RefactoringExecutionEvent.ABOUT_TO_UNDO) {
			isRefactoring= true;
			eventLogger.logRefactoringStarted();
		} else {
			isRefactoring= false;
			eventLogger.logRefactoringExecutionEvent(event);
		}
	}

}
