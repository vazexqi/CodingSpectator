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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.refactoring.descriptors.JavaRefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
@SuppressWarnings("restriction")
public class RefactoringLog {

	public enum LogType {
		PERFORMED, CANCELLED, UNAVAILABLE
	}

	IFileStore fileStore;

	private static final String GENERIC_VERSION_NUMBER= "1.0.0.qualifier";

	private static final String CANCELED_REFACTORINGS= "refactorings/canceled";

	private static final String PERFORMED_REFACTORINGS= "refactorings/performed";

	private static final String UNAVAILABLE_REFACTORINGS= "refactorings/unavailable";

	private static final String REFACTORING_HISTORY_LOCATION= Platform.getStateLocation(Platform.getBundle("org.eclipse.ltk.core.refactoring")).toOSString();

	private static Map<LogType, String> logTypeToDirectory= new HashMap<LogType, String>();

	static {
		logTypeToDirectory.put(LogType.CANCELLED, CANCELED_REFACTORINGS);
		logTypeToDirectory.put(LogType.PERFORMED, PERFORMED_REFACTORINGS);
		logTypeToDirectory.put(LogType.UNAVAILABLE, UNAVAILABLE_REFACTORINGS);
	}

	public RefactoringLog(LogType logType) {
		fileStore= getFileStore(logTypeToDirectory.get(logType));
	}

	public boolean exists() {
		return fileStore.fetchInfo().exists();
	}

	public void clean() throws CoreException {
		fileStore.delete(EFS.NONE, null);
	}

	private IFileStore getFileStore(String logDirectory) {
		return EFS.getLocalFileSystem().getStore(new Path(getRefactoringStorageLocation(logDirectory)));
	}

	private String getSystemFileSeparator() {
		return System.getProperty("file.separator");
	}

	private Version getFeatureVersion() {
		Bundle bundle= Platform.getBundle("edu.illinois.codingspectator.monitor");
		if (bundle != null)
			return bundle.getVersion();
		else
			return new Version(GENERIC_VERSION_NUMBER);
	}

	public String getRefactoringStorageLocation(String directory) {
		StringBuilder fullDirectory= new StringBuilder();
		fullDirectory.append(REFACTORING_HISTORY_LOCATION);
		fullDirectory.append(getSystemFileSeparator());
		fullDirectory.append(getFeatureVersion());

		String directorySeparator= "/";
		String[] directories= directory.split(directorySeparator);
		for (int i= 0; i < directories.length; i++) {
			fullDirectory.append(getSystemFileSeparator());
			fullDirectory.append(directories[i]);
		}

		return fullDirectory.toString();
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
