/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.listeners;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.history.IRefactoringExecutionListener;
import org.eclipse.ltk.core.refactoring.history.RefactoringExecutionEvent;

import edu.illinois.codingspectator.codingtracker.helpers.Debugger;
import edu.illinois.codingspectator.codingtracker.helpers.Messages;

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
			IJavaProject refactoredJavaProject= getRefactoredJavaProject(event);
			if (refactoredJavaProject != null && refactoredJavaProject.exists()) {
				operationRecorder.ensureOptionsAreCurrent(refactoredJavaProject);
			}
			operationRecorder.recordStartedRefactoring();
		} else if (eventType == RefactoringExecutionEvent.PERFORMED || eventType == RefactoringExecutionEvent.REDONE ||
				eventType == RefactoringExecutionEvent.UNDONE) {
			isRefactoring= false;
			operationRecorder.recordExecutedRefactoring(getRefactoringDescriptor(event), eventType);
		} else {
			//Actually, should never reach here, as all possible 6 types of events are checked above
			Exception e= new RuntimeException();
			Debugger.logExceptionToErrorLog(e, Messages.Recorder_UnrecognizedRefactoringType + eventType);
		}
	}

	private IJavaProject getRefactoredJavaProject(RefactoringExecutionEvent event) {
		IJavaProject refactoredJavaProject= null;
		String projectName= getRefactoringDescriptor(event).getProject();
		if (projectName != null && Path.EMPTY.isValidSegment(projectName)) {
			IProject project= ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			if (project.exists()) {
				refactoredJavaProject= JavaCore.create(project);
			}
		}
		return refactoredJavaProject;
	}

	private RefactoringDescriptor getRefactoringDescriptor(RefactoringExecutionEvent event) {
		RefactoringDescriptorProxy refactoringDescriptorProxy= event.getDescriptor();
		RefactoringDescriptor refactoringDescriptor= refactoringDescriptorProxy.requestDescriptor(new NullProgressMonitor());
		//Refactoring descriptor should never be null here (according to how RefactoringExecutionEvent object is constructed in 
		//RefactoringHistoryService)
		return refactoringDescriptor;
	}

}
