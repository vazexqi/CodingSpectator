/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.recording;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;

import edu.illinois.codingspectator.codingtracker.helpers.Debugger;
import edu.illinois.codingspectator.codingtracker.helpers.FileHelper;
import edu.illinois.codingspectator.codingtracker.helpers.Messages;
import edu.illinois.codingspectator.data.CodingSpectatorDataPlugin;

/**
 * 
 * @author Stas Negara
 * 
 */
public class KnownfilesRecorder {

	private static KnownfilesRecorder recorderInstance= null;

	private final Properties knownfiles= new Properties(); //Is thread-safe since SE 6

	private Map<String, String> currentWorkspaceOptions;

	private static final long REFRESH_INTERVAL= 7 * 24 * 60 * 60 * 1000; //Refresh knownfiles every 7 days

	private static final IPath CODINGTRACKER_PATH= Platform.getStateLocation(Platform.getBundle(Activator.PLUGIN_ID));

	private static final IPath KNOWNFILES_PATH= CODINGTRACKER_PATH.append(CodingSpectatorDataPlugin.getCodingSpectatorVersion().toString());

	private final File knownfilesFile= KNOWNFILES_PATH.append("knownfiles.txt").toFile();

	private final File workspaceOptionsFile= KNOWNFILES_PATH.append("workspaceOptions.txt").toFile();

	/**
	 * Very dangerous! Should be used ONLY for testing!
	 */
	public void reset() {
		knownfiles.clear();
		currentWorkspaceOptions.clear();
	}

	public static KnownfilesRecorder getInstance() {
		if (recorderInstance == null) {
			recorderInstance= new KnownfilesRecorder();
		}
		return recorderInstance;
	}

	private KnownfilesRecorder() {
		readPropertiesFromFile(knownfiles, knownfilesFile);
		refreshKnownfiles();
		Properties workspaceOptions= new Properties();
		readPropertiesFromFile(workspaceOptions, workspaceOptionsFile);
		currentWorkspaceOptions= getMap(workspaceOptions);
	}

	private void refreshKnownfiles() {
		long currentTime= System.currentTimeMillis();
		Iterator<Object> keysIterator= knownfiles.keySet().iterator();
		boolean hasChanged= false;
		while (keysIterator.hasNext()) {
			Object key= keysIterator.next();
			if (!isCVSEntriesPath(key.toString())) {
				String timestamp= knownfiles.getProperty(key.toString());
				if (currentTime - Long.valueOf(timestamp) > REFRESH_INTERVAL) {
					keysIterator.remove();
					hasChanged= true;
				}
			}
		}
		if (hasChanged) {
			recordKnownfiles();
		}
	}

	private boolean isCVSEntriesPath(String filePath) {
		return filePath.endsWith("/CVS/Entries");
	}

	public void recordKnownfiles() {
		Debugger.debug("recordKnownfiles");
		writePropertiesToFile(knownfiles, knownfilesFile);
	}

	private synchronized void readPropertiesFromFile(Properties properties, File file) {
		try {
			if (file.exists()) {
				properties.load(new FileInputStream(file));
			}
		} catch (Exception e) {
			Debugger.logExceptionToErrorLog(e, Messages.Recorder_ReadPropertiesFromFileException + file.getName());
		}
	}

	private synchronized void writePropertiesToFile(Properties properties, File file) {
		BufferedWriter bufferedWriter= null;
		try {
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}
			bufferedWriter= new BufferedWriter(new FileWriter(file, false));
			properties.store(bufferedWriter, null);
		} catch (IOException e) {
			Debugger.logExceptionToErrorLog(e, Messages.Recorder_WritePropertiesToFileException + file.getName());
		} finally {
			if (bufferedWriter != null) {
				try {
					bufferedWriter.close();
				} catch (IOException e) {
					//do nothing
				}
			}
		}
	}

	public boolean isFileKnown(IFile file) {
		return knownfiles.containsKey(FileHelper.getPortableFilePath(file));
	}

	void addKnownfile(IFile file) {
		knownfiles.setProperty(FileHelper.getPortableFilePath(file), String.valueOf(System.currentTimeMillis()));
	}

	public Object removeKnownfile(IFile file) {
		return knownfiles.remove(FileHelper.getPortableFilePath(file));
	}

	public synchronized void addCVSEntriesFile(IFile cvsEntriesSourceFile) {
		addKnownfile(cvsEntriesSourceFile);
		File cvsEntriesDestinationFile= getTrackedCVSEntriesFile(cvsEntriesSourceFile);
		cvsEntriesDestinationFile.getParentFile().mkdirs();
		BufferedWriter cvsEntriesDestinationFileWriter= null;
		try {
			cvsEntriesDestinationFileWriter= new BufferedWriter(new FileWriter(cvsEntriesDestinationFile, false));
			cvsEntriesDestinationFileWriter.append(FileHelper.getFileContent(cvsEntriesSourceFile.getLocation().toFile()));
			cvsEntriesDestinationFileWriter.flush();
		} catch (IOException e) {
			Debugger.logExceptionToErrorLog(e, Messages.Recorder_CVSEntriesCopyFailure);
		} finally {
			if (cvsEntriesDestinationFileWriter != null) {
				try {
					cvsEntriesDestinationFileWriter.close();
				} catch (IOException e) {
					//do nothing
				}
			}
		}
	}

	public static String getKnownFilesPath() {
		return KNOWNFILES_PATH.toOSString();
	}

	public File getTrackedCVSEntriesFile(IFile cvsEntriesSourceFile) {
		return KNOWNFILES_PATH.append(cvsEntriesSourceFile.getFullPath()).toFile();
	}

	boolean areWorkspaceOptionsCurrent(Map<String, String> workspaceOptions) {
		return currentWorkspaceOptions.equals(workspaceOptions);
	}

	void recordWorkspaceOptions(Map<String, String> workspaceOptions) {
		Debugger.debug("recordWorkspaceOptions");
		currentWorkspaceOptions= workspaceOptions;
		writePropertiesToFile(getProperties(currentWorkspaceOptions), workspaceOptionsFile);
	}

	boolean areProjectOptionsCurrent(String projectName, Map<String, String> projectOptions) {
		Properties trackedProjectOptions= new Properties();
		readPropertiesFromFile(trackedProjectOptions, getProjectOptionsFile(projectName));
		return getMap(trackedProjectOptions).equals(projectOptions);
	}

	void recordProjectOptions(String projectName, Map<String, String> projectOptions) {
		Debugger.debug("recordProjectOptions: " + projectName);
		writePropertiesToFile(getProperties(projectOptions), getProjectOptionsFile(projectName));
	}

	private File getProjectOptionsFile(String projectName) {
		return KNOWNFILES_PATH.append(projectName).append("projectOptions.txt").toFile();
	}

	private Properties getProperties(Map<String, String> map) {
		Properties properties= new Properties();
		properties.putAll(map);
		return properties;
	}

	private Map<String, String> getMap(Properties properties) {
		Hashtable<String, String> hashtable= new Hashtable<String, String>();
		for (Entry<Object, Object> entry : properties.entrySet()) {
			hashtable.put(entry.getKey().toString(), entry.getValue().toString());
		}
		return hashtable;
	}

}
