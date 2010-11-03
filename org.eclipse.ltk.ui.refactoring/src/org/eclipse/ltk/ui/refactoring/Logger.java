package org.eclipse.ltk.ui.refactoring;

import org.eclipse.core.runtime.Platform;

import org.eclipse.ltk.core.refactoring.IWatchedRefactoring;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistoryEvent;
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

	private static final String DEBUGGING_MODE= "DEBUG_MODE"; //$NON-NLS-1$

	private static boolean isInDebugMode() {
		return System.getenv(DEBUGGING_MODE) != null;
	}

	public static void logDebug(String debugInfo) {
		if (isInDebugMode()) {
			System.err.println(debugInfo);
		}
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

	static boolean doesMonitorUIExist() {
		return Platform.getBundle(MONITOR_UI) != null;
	}

}
