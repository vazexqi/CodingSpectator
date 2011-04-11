/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.recording;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;

import edu.illinois.codingspectator.codingtracker.helpers.CollectionHelper;
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

	enum FileProperties {
		ENCODING, TIMESTAMP
	}

	private static final String PROPERTIES_DELIMETER= ",";

	private static KnownfilesRecorder recorderInstance= null;

	private final Properties knownfiles; //Is thread-safe since SE 6

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
		knownfiles= readPropertiesFromFile(knownfilesFile);
		refreshKnownfiles();
		currentWorkspaceOptions= CollectionHelper.getMap(readPropertiesFromFile(workspaceOptionsFile));
	}

	private void refreshKnownfiles() {
		long currentTime= System.currentTimeMillis();
		Iterator<Object> keysIterator= knownfiles.keySet().iterator();
		boolean hasChanged= false;
		while (keysIterator.hasNext()) {
			String key= keysIterator.next().toString();
			if (!isCVSEntriesPath(key)) {
				String timestamp= getSpecificProperty(knownfiles.getProperty(key), FileProperties.TIMESTAMP);
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

	private String getSpecificProperty(String propertiesString, FileProperties property) {
		String[] properties= propertiesString.split(PROPERTIES_DELIMETER);
		switch (property) {
			case ENCODING:
				return properties[0];
			case TIMESTAMP:
				return properties[1];
		}
		return ""; //should not reach here
	}

	public void recordKnownfiles() {
		Debugger.debug("recordKnownfiles");
		writePropertiesToFile(knownfiles, knownfilesFile);
	}

	//TODO: See if reading and writing to Properties in this class, and to a file in FileHelper have sufficient similarities 
	//to be factored out in common methods.

	private synchronized Properties readPropertiesFromFile(File file) {
		Properties properties= new Properties();
		InputStreamReader inputStreamReader= null;
		try {
			if (file.exists()) {
				inputStreamReader= new InputStreamReader(new FileInputStream(file), FileHelper.UNIVERSAL_CHARSET);
				properties.load(inputStreamReader);
			}
		} catch (IOException e) {
			Debugger.logExceptionToErrorLog(e, Messages.Recorder_ReadPropertiesFromFileException + file.getName());
		} finally {
			if (inputStreamReader != null) {
				try {
					inputStreamReader.close();
				} catch (IOException e) {
					//do nothing
				}
			}
		}
		return properties;
	}

	private synchronized void writePropertiesToFile(Properties properties, File file) {
		BufferedWriter bufferedWriter= null;
		try {
			FileHelper.ensureFileExists(file);
			bufferedWriter= new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), FileHelper.UNIVERSAL_CHARSET));
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
		return isFileKnown(file, FileHelper.getCharsetNameForFile(file));
	}

	public boolean isFileKnown(IFile file, String charsetName) {
		String key= FileHelper.getPortableFilePath(file);
		String propertiesString= knownfiles.getProperty(key);
		if (propertiesString != null) {
			if (isCVSEntriesPath(key)) {
				return true;
			}
			return getSpecificProperty(propertiesString, FileProperties.ENCODING).equals(charsetName);
		}
		return false;
	}

	void addKnownfile(IFile file, String charsetName) {
		String propertiesString= charsetName + PROPERTIES_DELIMETER + String.valueOf(System.currentTimeMillis());
		knownfiles.setProperty(FileHelper.getPortableFilePath(file), propertiesString);
	}

	public Object removeKnownfile(IFile file) {
		return knownfiles.remove(FileHelper.getPortableFilePath(file));
	}

	public synchronized void addCVSEntriesFile(IFile cvsEntriesSourceFile) {
		addKnownfile(cvsEntriesSourceFile, FileHelper.getCharsetNameForFile(cvsEntriesSourceFile));
		File cvsEntriesDestinationFile= getTrackedCVSEntriesFile(cvsEntriesSourceFile);
		try {
			FileHelper.ensureFileExists(cvsEntriesDestinationFile);
			FileHelper.writeFileContent(cvsEntriesDestinationFile, FileHelper.readFileContent(cvsEntriesSourceFile), false);
		} catch (IOException e) {
			Debugger.logExceptionToErrorLog(e, Messages.Recorder_CVSEntriesCopyFailure);
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
		writePropertiesToFile(CollectionHelper.getProperties(currentWorkspaceOptions), workspaceOptionsFile);
	}

	boolean areProjectOptionsCurrent(String projectName, Map<String, String> projectOptions) {
		Properties trackedProjectOptions= readPropertiesFromFile(getProjectOptionsFile(projectName));
		return CollectionHelper.getMap(trackedProjectOptions).equals(projectOptions);
	}

	void recordProjectOptions(String projectName, Map<String, String> projectOptions) {
		Debugger.debug("recordProjectOptions: " + projectName);
		writePropertiesToFile(CollectionHelper.getProperties(projectOptions), getProjectOptionsFile(projectName));
	}

	private File getProjectOptionsFile(String projectName) {
		return getProjectFile(projectName, "projectOptions.txt");
	}

	boolean areReferencingProjectsCurrent(String projectName, Set<String> referencingProjectNames) {
		Properties trackedReferencingProjects= readPropertiesFromFile(getReferencingProjectsFile(projectName));
		return trackedReferencingProjects.keySet().equals(referencingProjectNames);
	}

	private File getReferencingProjectsFile(String projectName) {
		return getProjectFile(projectName, "referencingProjects.txt");
	}

	void recordReferencingProjects(String projectName, Set<String> referencingProjectNames) {
		Debugger.debug("recordReferencingProjectsForProject: " + projectName);
		writePropertiesToFile(CollectionHelper.getProperties(referencingProjectNames), getReferencingProjectsFile(projectName));
	}

	private File getProjectFile(String projectName, String fileName) {
		return KNOWNFILES_PATH.append(projectName).append(fileName).toFile();
	}

}
