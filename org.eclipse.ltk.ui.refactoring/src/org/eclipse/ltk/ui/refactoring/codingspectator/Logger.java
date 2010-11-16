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

	public static RefactoringDescriptor createRefactoringDescriptor(RefactoringStatus status, Refactoring refactoring) {
		if (!(refactoring instanceof IWatchedRefactoring))
			return null;

		IWatchedRefactoring watchedRefactoring= (IWatchedRefactoring)refactoring;
		if (!(watchedRefactoring.isWatched()))
			return null;

		return watchedRefactoring.getSimpleRefactoringDescriptor(status);
	}

	public static void logRefactoringDescriptor(int refactoringEventType, RefactoringDescriptor refactoringDescriptor) {
		if (refactoringDescriptor == null)
			return;

		if (!Logger.doesMonitorUIExist()) {
			return;
		}

		logDebug(refactoringDescriptor.toString());

		// Wrap it into a refactoring descriptor proxy
		RefactoringDescriptorProxy proxy= new RefactoringDescriptorProxyAdapter(refactoringDescriptor);

		// Wrap it into a refactoring descriptor event using proxy
		RefactoringHistoryEvent event= new RefactoringHistoryEvent(RefactoringCore.getHistoryService(), refactoringEventType, proxy);

		// Call RefactoringHistorySerializer to persist
		RefactoringHistorySerializer serializer= new RefactoringHistorySerializer();
		serializer.historyNotification(event);
	}

	public static void logRefactoringEvent(int refactoringEventType, RefactoringStatus status, Refactoring refactoring) {
		RefactoringDescriptor refactoringDescriptor= createRefactoringDescriptor(status, refactoring);
		logRefactoringDescriptor(refactoringEventType, refactoringDescriptor);
	}

	//FIXME
	public static void logBasicRefactoringEvent(int refactoringEventType, RefactoringStatus status, Refactoring refactoring) {
		if (!Logger.doesMonitorUIExist()) {
			return;
		}
		if (!(refactoring instanceof IWatchedRefactoring))
			return;

		IWatchedRefactoring watchedRefactoring= (IWatchedRefactoring)refactoring;
		if (!(watchedRefactoring.isWatched()))
			return;

		RefactoringDescriptor refactoringDescriptor= getBasicRefactoringDescriptor(status);
		logRefactoringDescriptor(refactoringEventType, refactoringDescriptor);
	}

	public static RefactoringDescriptor getBasicRefactoringDescriptor(RefactoringStatus status) {
		Map arguments= new HashMap();
		//arguments.put(RefactoringDescriptor.ATTRIBUTE_CODE_SNIPPET, getCodeSnippet());
		//arguments.put(RefactoringDescriptor.ATTRIBUTE_SELECTION, getSelection());
		arguments.put(RefactoringDescriptor.ATTRIBUTE_STATUS, status.toString());

		String BASIC_REFACTORING_DESCRIPTOR_DESCRIPTION= "CODINGSPECTATOR: RefactoringDescriptor from failed CheckInitialConditions"; //$NON-NLS-1$

		return new DefaultRefactoringDescriptor("JAVA_REFACTORING", "JAVA_PROJECT", BASIC_REFACTORING_DESCRIPTOR_DESCRIPTION, "", arguments, RefactoringDescriptor.NONE);
	}

}
