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

	private static final String CANCELED_REFACTORINGS= "refactorings/canceled";

	private static final String PERFORMED_REFACTORINGS= "refactorings/performed";

	private static final String UNAVAILABLE_REFACTORINGS= "refactorings/unavailable";

	private static final IPath REFACTORING_HISTORY_LOCATION= CodingSpectatorDataPlugin.getVersionedStorageLocation();

	private static Map<LogType, String> logTypeToDirectory= new HashMap<LogType, String>();

	static {
		logTypeToDirectory.put(LogType.CANCELLED, CANCELED_REFACTORINGS);
		logTypeToDirectory.put(LogType.PERFORMED, PERFORMED_REFACTORINGS);
		logTypeToDirectory.put(LogType.UNAVAILABLE, UNAVAILABLE_REFACTORINGS);
	}

	public RefactoringLog(IPath pathToHistoryFolder) {
		fileStore= EFS.getLocalFileSystem().getStore(pathToHistoryFolder);
	}

	public RefactoringLog(LogType logType) {
		this((logType == LogType.ECLIPSE) ? RefactoringCorePlugin.getDefault().getStateLocation().append(".refactorings") : getRefactoringStorageLocation(logTypeToDirectory.get(logType)));
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
			refactoringDescriptors.add((JavaRefactoringDescriptor)refactoringHistoryManager.requestDescriptor(refactoringDescriptorProxy, new NullProgressMonitor()));
		}
		return refactoringDescriptors;
	}
}
