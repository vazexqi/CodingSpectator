/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;
import org.eclipse.ltk.internal.core.refactoring.RefactoringCorePlugin;
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

	IFileStore fileStore;

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
		fileStore= EFS.getLocalFileSystem().getStore(pathToHistoryFolder);
	}

	public RefactoringLog(LogType logType) {
		this((logType == LogType.ECLIPSE) ? RefactoringCorePlugin.getDefault().getStateLocation().append(".refactorings") : getRefactoringStorageLocation("refactorings/"
				+ toString(logType)));
	}

	public String getPathToRefactoringHistoryFolder() {
		return fileStore.toURI().getPath();
	}

	public boolean exists() {
		return fileStore.fetchInfo().exists();
	}

	public void clean() throws CoreException {
		fileStore.delete(EFS.NONE, null);
	}

	public static IPath getRefactoringStorageLocation(String directory) {
		return REFACTORING_HISTORY_LOCATION.append(directory);
	}

	public Collection<JavaRefactoringDescriptor> getRefactoringDescriptors(String javaProjectName) {
		RefactoringHistoryManager refactoringHistoryManager= new RefactoringHistoryManager(fileStore.getChild(javaProjectName), javaProjectName);
		RefactoringHistory refactoringHistory= refactoringHistoryManager.readRefactoringHistory(0, Long.MAX_VALUE, new NullProgressMonitor());
		RefactoringDescriptorProxy[] refactoringDescriptorProxies= refactoringHistory.getDescriptors();
		Collection<JavaRefactoringDescriptor> refactoringDescriptors= new ArrayList<JavaRefactoringDescriptor>();
		for (RefactoringDescriptorProxy refactoringDescriptorProxy : refactoringDescriptorProxies) {
			JavaRefactoringDescriptor javaRefactoringDescriptor= (JavaRefactoringDescriptor)refactoringHistoryManager.requestDescriptor(refactoringDescriptorProxy, new NullProgressMonitor());
			if (javaRefactoringDescriptor != null) {
				refactoringDescriptors.add(javaRefactoringDescriptor);
			}
		}
		return refactoringDescriptors;
	}
}
