/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.refactoringproblems.logger;


import org.eclipse.core.internal.events.ResourceChangeEvent;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.internal.ui.viewsupport.IProblemChangedListener;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.history.IRefactoringExecutionListener;
import org.eclipse.ltk.core.refactoring.history.RefactoringExecutionEvent;
import org.eclipse.ui.IStartup;

/**
 * 
 * @author Balaji Ambresh Rajkumar
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
@SuppressWarnings("restriction")
public class RefactoringProblemsListener implements IStartup, IProblemChangedListener, IResourceChangeListener, IRefactoringExecutionListener {


	private RefactoringProblemsLogger logger;

	boolean refactoringInSession;

	public RefactoringProblemsListener() {
		logger= new RefactoringProblemsLogger();
	}

	@Override
	public void earlyStartup() {
//		JavaPlugin.getDefault().getProblemMarkerManager().addListener(this);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_BUILD);
		RefactoringCore.getHistoryService().addExecutionListener(this);
	}

	@Override
	public void problemsChanged(IResource[] changedResources, boolean isMarkerChange) {
//		TODO: Handle the case where "Build Automatically" is turned off
//		for (IResource resource : changedResources) {
//			System.err.println(resource);
//		}
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		//TODO: Do we need a time out in case the refactoring crashes and never calls RefactoringExecutionEvent.PERFORMED 
		if (refactoringInSession) {
			ResourceChangeEvent cEvent= (ResourceChangeEvent)event;
			IMarkerDelta[] findMarkerDeltas= cEvent.findMarkerDeltas(IMarker.PROBLEM, true);
			logger.logRefactoringProblems(findMarkerDeltas);
			refactoringInSession= false;
		}
	}

	@Override
	public void executionNotification(RefactoringExecutionEvent event) {
		//TODO: should we listen to ABOUT_TO_UNDO and ABOUT_TO_REDO
		if (event.getEventType() == RefactoringExecutionEvent.ABOUT_TO_PERFORM)
			refactoringInSession= true;
	}
}
