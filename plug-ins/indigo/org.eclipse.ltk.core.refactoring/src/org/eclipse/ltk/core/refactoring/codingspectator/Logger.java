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

	public static IClearable clearable= new NullClearable();

	public static final String NAVIGATION_HISTORY_ATTRIBUTE= "navigation-history"; //$NON-NLS-1$

	private static final String MONITOR_UI= "edu.illinois.codingspectator.monitor.ui"; //$NON-NLS-1$

	public static void logDebug(String debugInfo) {
		if (RunningModes.isInDebugMode()) {
			System.err.println(debugInfo);
		}
	}

	private static boolean doesMonitorUIExist() {
		return Platform.getBundle(MONITOR_UI) != null;
	}

	public static RefactoringDescriptor createRefactoringDescriptor(RefactoringStatus status, Refactoring refactoring) {
		if (isWatched(refactoring))
			return convertToWatchedRefactoring(refactoring).getSimpleRefactoringDescriptor(status);
		return null;
	}

	private static IWatchedRefactoring convertToWatchedRefactoring(Refactoring refactoring) {
		if (!(refactoring instanceof IWatchedRefactoring))
			return null;

		IWatchedRefactoring watchedRefactoring= (IWatchedRefactoring)refactoring;
		if (!(watchedRefactoring.isWatched()))
			return null;

		return watchedRefactoring;
	}

	private static boolean isWatched(Refactoring refactoring) {
		return convertToWatchedRefactoring(refactoring) != null;
	}

	private static void logRefactoringDescriptor(int refactoringEventType, RefactoringDescriptor refactoringDescriptor) {
		if (refactoringDescriptor == null)
			return;

		logDebug(refactoringDescriptor.toString());
		serializeRefactoringEvent(refactoringDescriptor, refactoringEventType);
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

	private static RefactoringDescriptor augmentedDescriptor(RefactoringDescriptor refactoringDescriptor, String key, String value) {
		HashMap augmentedArguments= new HashMap();
		augmentedArguments.put(key, value);
		return refactoringDescriptor.cloneByAugmenting(augmentedArguments);
	}

	private static RefactoringDescriptor appendNavigationHistory(NavigationHistory navigationHistory, RefactoringDescriptor refactoringDescriptor) {
		if (navigationHistory != null) {
			refactoringDescriptor= augmentedDescriptor(refactoringDescriptor, NAVIGATION_HISTORY_ATTRIBUTE, navigationHistory.toString());
		}
		return refactoringDescriptor;
	}

	public static void logUnavailableRefactoringEvent(String refactoring, String project, CodeSnippetInformation info, String errorMessage) {
		RefactoringDescriptor refactoringDescriptor= getBasicRefactoringDescriptor(refactoring, project, info, errorMessage);
		logDebug(refactoringDescriptor.toString());
		serializeRefactoringEvent(refactoringDescriptor, RefactoringHistoryEvent.CODINGSPECTATOR_REFACTORING_UNAVAILABLE);
	}

	private static void serializeRefactoringEvent(RefactoringDescriptor refactoringDescriptor, int refactoringEventType) {
		// To disable CodingSpectator on target platforms, we instruct the users to remove the monitor.ui plugin from their target platform.
		// Therefore, if the monitor.ui plug-in does not exist CodingSpectator won't log any refactorings. 
		if (!Logger.doesMonitorUIExist()) {
			return;
		}

		refactoringDescriptor= augmentedDescriptor(refactoringDescriptor, RefactoringDescriptor.CAPTURED_BY_CODINGSPECTATOR_ATTRIBUTE, String.valueOf(true));

		setTheTimestampOfRefactoringIfNotAlreadySet(refactoringDescriptor);

		// Wrap it into a refactoring descriptor proxy
		RefactoringDescriptorProxy proxy= new RefactoringDescriptorProxyAdapter(refactoringDescriptor);

		// Wrap it into a refactoringdecriptor event using proxy
		RefactoringHistoryEvent event= new RefactoringHistoryEvent(RefactoringCore.getHistoryService(), refactoringEventType, proxy);

		// Call RefactoringHistorySerializer to persist
		RefactoringHistorySerializer serializer= new RefactoringHistorySerializer();
		serializer.historyNotification(event);

		clearable.clearData();
	}

	private static void setTheTimestampOfRefactoringIfNotAlreadySet(RefactoringDescriptor refactoringDescriptor) {
		if (refactoringDescriptor.getTimeStamp() == -1) {
			refactoringDescriptor.setTimeStamp(System.currentTimeMillis());
		}
	}

	private static RefactoringDescriptor getBasicRefactoringDescriptor(String refactoring, String project, CodeSnippetInformation info, String errorMessage) {
		Map arguments= new HashMap();
		info.insertIntoMap(arguments);
		arguments.put(RefactoringDescriptor.ATTRIBUTE_STATUS, errorMessage);

		String BASIC_REFACTORING_DESCRIPTOR_DESCRIPTION= "CODINGSPECTATOR: RefactoringDescriptor from an unavailable refactoring"; //$NON-NLS-1$

		// We used DefaultRefactoringDescriptor instead of a specific JavaRefactoringDescriptor even though we know which Java refactoring it is because it is not always possible to construct 
		// a JavaRefactoringDescriptor. A JavaRefactoringDescritptor expects more information, and that information cannot be NULL (it explicitly checks for those and fails on assertion).
		return new DefaultRefactoringDescriptor(refactoring, project, BASIC_REFACTORING_DESCRIPTOR_DESCRIPTION, null, arguments, RefactoringDescriptor.NONE);
	}

}
