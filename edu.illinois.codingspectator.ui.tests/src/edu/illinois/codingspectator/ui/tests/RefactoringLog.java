/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import edu.illinois.codingspectator.data.CodingSpectatorDataPlugin;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class RefactoringLog {

	public enum LogType {
		PERFORMED, CANCELLED, UNAVAILABLE
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
		return EFS.getLocalFileSystem().getStore(getRefactoringStorageLocation(logDirectory));
	}

	public IPath getRefactoringStorageLocation(String directory) {
		return REFACTORING_HISTORY_LOCATION.append(directory);
	}

}
