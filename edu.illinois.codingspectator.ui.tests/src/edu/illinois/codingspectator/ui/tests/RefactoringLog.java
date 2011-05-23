/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCorePlugin;
import org.eclipse.ltk.internal.core.refactoring.history.DefaultRefactoringDescriptor;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringContributionManager;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryManager;

import edu.illinois.codingspectator.data.CodingSpectatorDataPlugin;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
@SuppressWarnings("restriction")
public class RefactoringLog {

	public enum LogType {
		ECLIPSE, PERFORMED, CANCELLED, UNAVAILABLE
	}

	EFSFile historyFolder;

	private static final String ECLIPSE_REFACTORINGS= "eclipse";

	private static final String CANCELED_REFACTORINGS= "canceled";

	private static final String PERFORMED_REFACTORINGS= "performed";

	private static final String UNAVAILABLE_REFACTORINGS= "unavailable";

	private static final IPath REFACTORING_HISTORY_LOCATION= CodingSpectatorDataPlugin.getVersionedStorageLocation();

	private static Map<LogType, String> logTypeToString= new HashMap<LogType, String>();

	private static Map<String, LogType> stringToLogType= new HashMap<String, LogType>();

	static {
		logTypeToString.put(LogType.ECLIPSE, ECLIPSE_REFACTORINGS);
		logTypeToString.put(LogType.CANCELLED, CANCELED_REFACTORINGS);
		logTypeToString.put(LogType.PERFORMED, PERFORMED_REFACTORINGS);
		logTypeToString.put(LogType.UNAVAILABLE, UNAVAILABLE_REFACTORINGS);

		for (Map.Entry<LogType, String> logTypeToStringEntry : logTypeToString.entrySet()) {
			stringToLogType.put(logTypeToStringEntry.getValue(), logTypeToStringEntry.getKey());
		}
	}

	public static Set<LogType> getLogTypes() {
		return Collections.unmodifiableSet(logTypeToString.keySet());
	}

	public static boolean isLogType(String logName) {
		return logTypeToString.values().contains(logName);
	}

	public static LogType toLogType(String logTypeString) {
		return stringToLogType.get(logTypeString);
	}

	public static String toString(LogType logType) {
		return logTypeToString.get(logType);
	}

	public RefactoringLog(IPath pathToHistoryFolder) {
		historyFolder= new EFSFile(pathToHistoryFolder);
	}

	public RefactoringLog(LogType logType) {
		this((logType == LogType.ECLIPSE) ? RefactoringCorePlugin.getDefault().getStateLocation().append(".refactorings") : getRefactoringStorageLocation("refactorings/"
				+ toString(logType)));
	}

	public String getPathToRefactoringHistoryFolder() {
		return historyFolder.getPath().toOSString();
	}

	public boolean exists() {
		return historyFolder.exists();
	}

	public void delete() throws CoreException {
		historyFolder.delete();
	}

	public static IPath getRefactoringStorageLocation(String directory) {
		return REFACTORING_HISTORY_LOCATION.append(directory);
	}

	public Collection<CapturedRefactoringDescriptor> getRefactoringDescriptors(String javaProjectName) {
		RefactoringContributionManager.getInstance().setMustCreateDefaultRefactoringDescriptor(true);
		RefactoringHistoryManager refactoringHistoryManager= new RefactoringHistoryManager(historyFolder.append(javaProjectName).getFileStore(), javaProjectName);
		RefactoringHistory refactoringHistory= refactoringHistoryManager.readRefactoringHistory(0, Long.MAX_VALUE, new NullProgressMonitor());
		RefactoringDescriptorProxy[] refactoringDescriptorProxies= refactoringHistory.getDescriptors();
		Collection<CapturedRefactoringDescriptor> refactoringDescriptors= new ArrayList<CapturedRefactoringDescriptor>();
		for (RefactoringDescriptorProxy refactoringDescriptorProxy : refactoringDescriptorProxies) {
			DefaultRefactoringDescriptor refactoringDescriptor= (DefaultRefactoringDescriptor)refactoringHistoryManager.requestDescriptor(refactoringDescriptorProxy, new NullProgressMonitor());
			if (refactoringDescriptor != null) {
				refactoringDescriptors.add(new CapturedRefactoringDescriptor(refactoringDescriptor));
			}
		}
		return refactoringDescriptors;
	}

	public void copy(RefactoringLog destination) throws CoreException {
		historyFolder.copyTo(destination.historyFolder);
	}

}
