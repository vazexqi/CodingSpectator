package org.eclipse.ltk.ui.refactoring.codingspectator;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Platform;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.codingspectator.IWatchedRefactoring;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistoryEvent;
import org.eclipse.ltk.internal.core.refactoring.history.DefaultRefactoringDescriptor;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringDescriptorProxyAdapter;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistorySerializer;

/**
 * Logs refactoring descriptors(to file) and debugging information (to console)
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class Logger {

	private static final String MONITOR_UI= "edu.illinois.codingspectator.monitor.ui"; //$NON-NLS-1$

	public static void logDebug(String debugInfo) {
		if (RunningModes.isInDebugMode()) {
			System.err.println(debugInfo);
		}
	}

	static boolean doesMonitorUIExist() {
		return Platform.getBundle(MONITOR_UI) != null;
	}

	public static void logRefactoringEvent(int refactoringEventType, RefactoringStatus status, Refactoring refactoring) {
		if (!Logger.doesMonitorUIExist()) {
			return;
		}
		if (!(refactoring instanceof IWatchedRefactoring))
			return;

		IWatchedRefactoring watchedRefactoring= (IWatchedRefactoring)refactoring;
		if (!(watchedRefactoring.isWatched()))
			return;

		RefactoringDescriptor refactoringDescriptor= watchedRefactoring.getSimpleRefactoringDescriptor(status);
		logDebug(refactoringDescriptor.toString());

		// Wrap it into a refactoring descriptor proxy
		RefactoringDescriptorProxy proxy= new RefactoringDescriptorProxyAdapter(refactoringDescriptor);

		// Wrap it into a refactoringdecriptor event using proxy
		RefactoringHistoryEvent event= new RefactoringHistoryEvent(RefactoringCore.getHistoryService(), refactoringEventType, proxy);

		// Call RefactoringHistorySerializer to persist
		RefactoringHistorySerializer serializer= new RefactoringHistorySerializer();
		serializer.historyNotification(event);
	}

	public static void logDisallowedRefactoringEvent(Refactoring refactoring, String project, String selectionInformation, String errorMessage) {
		RefactoringDescriptor refactoringDescriptor= getBasicRefactoringDescriptor(refactoring, project, selectionInformation, errorMessage);
		logDebug(refactoringDescriptor.toString());

		// Wrap it into a refactoring descriptor proxy
		RefactoringDescriptorProxy proxy= new RefactoringDescriptorProxyAdapter(refactoringDescriptor);

		// Wrap it into a refactoringdecriptor event using proxy
		RefactoringHistoryEvent event= new RefactoringHistoryEvent(RefactoringCore.getHistoryService(), RefactoringHistoryEvent.CODINGSPECTATOR_REFACTORING_DISALLOWED, proxy);

		// Call RefactoringHistorySerializer to persist
		RefactoringHistorySerializer serializer= new RefactoringHistorySerializer();
		serializer.historyNotification(event);
	}

	private static RefactoringDescriptor getBasicRefactoringDescriptor(Refactoring refactoring, String project, String selectionInformation, String errorMessage) {
		Map arguments= new HashMap();
		arguments.put(RefactoringDescriptor.ATTRIBUTE_SELECTION, selectionInformation);
		arguments.put(RefactoringDescriptor.ATTRIBUTE_STATUS, errorMessage);

		String BASIC_REFACTORING_DESCRIPTOR_DESCRIPTION= "CODINGSPECTATOR: RefactoringDescriptor from failed CheckInitialConditions"; //$NON-NLS-1$

		return new DefaultRefactoringDescriptor(refactoring.getName(), project, BASIC_REFACTORING_DESCRIPTOR_DESCRIPTION, null, arguments, RefactoringDescriptor.NONE);
	}
}
