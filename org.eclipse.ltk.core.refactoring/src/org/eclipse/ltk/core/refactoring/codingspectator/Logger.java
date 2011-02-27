package org.eclipse.ltk.core.refactoring.codingspectator;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Platform;

import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
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

	private static final String NAVIGATION_HISTORY_ATTRIBUTE= "navigationHistory"; //$NON-NLS-1$

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
		if (isWatched(refactoring))
			return convertToWatchedRefactoring(refactoring).getSimpleRefactoringDescriptor(status);
		return null;
	}

	public static IWatchedRefactoring convertToWatchedRefactoring(Refactoring refactoring) {
		if (!(refactoring instanceof IWatchedRefactoring))
			return null;

		IWatchedRefactoring watchedRefactoring= (IWatchedRefactoring)refactoring;
		if (!(watchedRefactoring.isWatched()))
			return null;

		return watchedRefactoring;
	}

	public static boolean isWatched(Refactoring refactoring) {
		return convertToWatchedRefactoring(refactoring) != null;
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
		logRefactoringEvent(refactoringEventType, status, refactoring, null);
	}

	public static void logRefactoringEvent(int refactoringEventType, RefactoringStatus status, Refactoring refactoring, NavigationHistory navigationHistory) {
		if (isWatched(refactoring)) {
			RefactoringDescriptor refactoringDescriptor= createRefactoringDescriptor(status, refactoring);
			appendThenLog(refactoringEventType, navigationHistory, refactoringDescriptor);
		}
	}

	private static void appendThenLog(int refactoringEventType, NavigationHistory navigationHistory, RefactoringDescriptor refactoringDescriptor) {
		refactoringDescriptor= appendNavigationHistory(navigationHistory, refactoringDescriptor);
		logRefactoringDescriptor(refactoringEventType, refactoringDescriptor);
	}

	public static void logRefactoringEvent(int refactoringEventType, RefactoringDescriptor refactoringDescriptor, NavigationHistory navigationHistory) {
		if (refactoringDescriptor != null) {
			appendThenLog(refactoringEventType, navigationHistory, refactoringDescriptor);
		}
	}

	public static RefactoringDescriptor appendNavigationHistory(NavigationHistory navigationHistory, RefactoringDescriptor refactoringDescriptor) {
		if (navigationHistory != null) {
			HashMap augmentedArguments= new HashMap();
			augmentedArguments.put(NAVIGATION_HISTORY_ATTRIBUTE, navigationHistory.toString());
			refactoringDescriptor= refactoringDescriptor.cloneByAugmenting(augmentedArguments);
		}
		return refactoringDescriptor;
	}

	public static void logUnavailableRefactoringEvent(String refactoring, String project, String selectionInformation, String errorMessage) {
		RefactoringDescriptor refactoringDescriptor= getBasicRefactoringDescriptor(refactoring, project, selectionInformation, null, null, errorMessage);
		logDebug(refactoringDescriptor.toString());

		// Wrap it into a refactoring descriptor proxy
		RefactoringDescriptorProxy proxy= new RefactoringDescriptorProxyAdapter(refactoringDescriptor);

		// Wrap it into a refactoringdecriptor event using proxy
		RefactoringHistoryEvent event= new RefactoringHistoryEvent(RefactoringCore.getHistoryService(), RefactoringHistoryEvent.CODINGSPECTATOR_REFACTORING_UNAVAILABLE, proxy);

		// Call RefactoringHistorySerializer to persist
		RefactoringHistorySerializer serializer= new RefactoringHistorySerializer();
		serializer.historyNotification(event);
	}
	
	public static void logUnavailableRefactoringEvent(String refactoring, String project, String selection, String codeSnippet, String selectionOffset, String errorMessage) {
		RefactoringDescriptor refactoringDescriptor= getBasicRefactoringDescriptor(refactoring, project, selection, codeSnippet, selectionOffset, errorMessage);
		logDebug(refactoringDescriptor.toString());

		// Wrap it into a refactoring descriptor proxy
		RefactoringDescriptorProxy proxy= new RefactoringDescriptorProxyAdapter(refactoringDescriptor);

		// Wrap it into a refactoringdecriptor event using proxy
		RefactoringHistoryEvent event= new RefactoringHistoryEvent(RefactoringCore.getHistoryService(), RefactoringHistoryEvent.CODINGSPECTATOR_REFACTORING_UNAVAILABLE, proxy);

		// Call RefactoringHistorySerializer to persist
		RefactoringHistorySerializer serializer= new RefactoringHistorySerializer();
		serializer.historyNotification(event);
	}

	private static RefactoringDescriptor getBasicRefactoringDescriptor(String refactoring, String project, String selection, String codeSnippet, String selectionOffset, String errorMessage) {
		Map arguments= new HashMap();
		arguments.put(RefactoringDescriptor.ATTRIBUTE_SELECTION, selection);
		arguments.put(RefactoringDescriptor.ATTRIBUTE_STATUS, errorMessage);
		arguments.put(RefactoringDescriptor.ATTRIBUTE_CODE_SNIPPET, codeSnippet);
		arguments.put(RefactoringDescriptor.ATTRIBUTE_SELECTION_OFFSET, selectionOffset);

		String BASIC_REFACTORING_DESCRIPTOR_DESCRIPTION= "CODINGSPECTATOR: RefactoringDescriptor from an unavailable refactoring"; //$NON-NLS-1$

		return new DefaultRefactoringDescriptor(refactoring, project, BASIC_REFACTORING_DESCRIPTOR_DESCRIPTION, null, arguments, RefactoringDescriptor.NONE);
	}
}
