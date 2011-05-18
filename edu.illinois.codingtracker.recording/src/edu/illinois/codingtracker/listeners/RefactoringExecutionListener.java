/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.listeners;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.history.IRefactoringExecutionListener;
import org.eclipse.ltk.core.refactoring.history.RefactoringExecutionEvent;

import edu.illinois.codingtracker.helpers.ResourceHelper;

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
		if (isBeginRefactoring(eventType)) {
			isRefactoring= true;
			trackProjectsAffectedByRefactoring(event);
			operationRecorder.recordStartedRefactoring(getRefactoringDescriptor(event), eventType);
		}
	}

	private boolean isBeginRefactoring(int eventType) {
		return eventType == RefactoringExecutionEvent.ABOUT_TO_PERFORM || eventType == RefactoringExecutionEvent.ABOUT_TO_REDO ||
				eventType == RefactoringExecutionEvent.ABOUT_TO_UNDO;
	}

	private void trackProjectsAffectedByRefactoring(RefactoringExecutionEvent event) {
		IProject refactoredProject= getRefactoredProject(event);
		if (refactoredProject != null && refactoredProject.exists()) {
			Set<IJavaProject> affectedJavaProjects= new HashSet<IJavaProject>();
			affectedJavaProjects.add(getJavaProject(refactoredProject));
			IProject[] referencingProjects= refactoredProject.getReferencingProjects();
			for (IProject referencingProject : referencingProjects) {
				if (referencingProject.exists()) { //Actually, should always exist here
					affectedJavaProjects.add(getJavaProject(referencingProject));
				}
			}
			operationRecorder.ensureReferencingProjectsAreCurrent(refactoredProject.getName(), getProjectNames(referencingProjects));
			operationRecorder.ensureOptionsAreCurrent(affectedJavaProjects);
		}
	}

	private IProject getRefactoredProject(RefactoringExecutionEvent event) {
		IProject refactoredProject= null;
		String projectName= getRefactoringDescriptor(event).getProject();
		if (projectName != null && Path.EMPTY.isValidSegment(projectName)) {
			refactoredProject= ResourceHelper.getWorkspaceRoot().getProject(projectName);
		}
		return refactoredProject;
	}

	private RefactoringDescriptor getRefactoringDescriptor(RefactoringExecutionEvent event) {
		RefactoringDescriptorProxy refactoringDescriptorProxy= event.getDescriptor();
		RefactoringDescriptor refactoringDescriptor= refactoringDescriptorProxy.requestDescriptor(new NullProgressMonitor());
		//Refactoring descriptor should never be null here (according to how RefactoringExecutionEvent object is constructed in 
		//RefactoringHistoryService)
		return refactoringDescriptor;
	}

	private IJavaProject getJavaProject(IProject project) {
		return JavaCore.create(project);
	}

	private Set<String> getProjectNames(IProject[] projects) {
		Set<String> projectNames= new HashSet<String>();
		for (IProject project : projects) {
			projectNames.add(project.getName());
		}
		return projectNames;
	}

}
