/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.recording;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;

import edu.illinois.codingspectator.data.CodingSpectatorDataPlugin;
import edu.illinois.codingtracker.helpers.CollectionHelper;
import edu.illinois.codingtracker.helpers.Debugger;
import edu.illinois.codingtracker.helpers.Messages;
import edu.illinois.codingtracker.helpers.ResourceHelper;
import edu.illinois.codingtracker.helpers.StringHelper;

/**
 * 
 * @author Stas Negara
 * 
 */
public class KnownFilesRecorder {

	enum FileProperties {
		ENCODING, TIMESTAMP
	}

	private static final String PROPERTIES_DELIMETER= ",";

	private static KnownFilesRecorder recorderInstance= null;

	private final Properties knownFiles; //Is thread-safe since SE 6

	private Map<String, String> currentWorkspaceOptions;

	private static final long REFRESH_INTERVAL= 7 * 24 * 60 * 60 * 1000; //Refresh known files every 7 days

	private static final IPath CODINGTRACKER_PATH= Platform.getStateLocation(Platform.getBundle(Activator.PLUGIN_ID));

	private static final IPath KNOWNFILES_PATH= CODINGTRACKER_PATH.append(CodingSpectatorDataPlugin.getCodingSpectatorVersion().toString());

	private final File knownFilesFile= KNOWNFILES_PATH.append("knownFiles.txt").toFile();

	private final File workspaceOptionsFile= KNOWNFILES_PATH.append("workspaceOptions.txt").toFile();

	/**
	 * Very dangerous! Should be used ONLY for testing!
	 */
	public void reset() {
		knownFiles.clear();
		currentWorkspaceOptions.clear();
	}

	public static KnownFilesRecorder getInstance() {
		if (recorderInstance == null) {
			recorderInstance= new KnownFilesRecorder();
		}
		return recorderInstance;
	}

	private KnownFilesRecorder() {
		knownFiles= readPropertiesFromFile(knownFilesFile);
		refreshKnownFiles();
		currentWorkspaceOptions= CollectionHelper.getMap(readPropertiesFromFile(workspaceOptionsFile));
	}

	private void refreshKnownFiles() {
		long currentTime= System.currentTimeMillis();
		Iterator<Object> keysIterator= knownFiles.keySet().iterator();
		boolean hasChanged= false;
		while (keysIterator.hasNext()) {
			String key= keysIterator.next().toString();
			if (!isCVSEntriesPath(key)) {
				String timestamp= getSpecificProperty(knownFiles.getProperty(key), FileProperties.TIMESTAMP);
				if (currentTime - Long.valueOf(timestamp) > REFRESH_INTERVAL) {
					keysIterator.remove();
					hasChanged= true;
				}
			}
		}
		if (hasChanged) {
			recordKnownFiles();
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

	public void recordKnownFiles() {
		Debugger.debug("recordKnownFiles");
		writePropertiesToFile(knownFiles, knownFilesFile);
	}

	//TODO: See if reading and writing to Properties in this class, and to a file in FileHelper have sufficient similarities 
	//to be factored out in common methods.

	private synchronized Properties readPropertiesFromFile(File file) {
		Properties properties= new Properties();
		InputStreamReader inputStreamReader= null;
		try {
			if (file.exists()) {
				inputStreamReader= new InputStreamReader(new FileInputStream(file), ResourceHelper.UNIVERSAL_CHARSET);
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
			ResourceHelper.ensureFileExists(file);
			bufferedWriter= new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), ResourceHelper.UNIVERSAL_CHARSET));
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

	public boolean isFileKnown(IFile file, boolean shouldMatchEncoding) {
		return isFileKnown(file, ResourceHelper.getCharsetNameForFile(file), shouldMatchEncoding);
	}

	public boolean isFileKnown(IFile file, String charsetName, boolean shouldMatchEncoding) {
		String propertiesString= knownFiles.getProperty(getKeyForResource(file));
		if (propertiesString != null) {
			if (shouldMatchEncoding) {
				//Compare Charsets rather than string representations to account for aliases.
				Charset fileCharset= ResourceHelper.getCharsetForNameOrDefault(charsetName);
				Charset storedCharset= ResourceHelper.getCharsetForNameOrDefault(getSpecificProperty(propertiesString, FileProperties.ENCODING));
				return fileCharset.equals(storedCharset);
			} else {
				return true;
			}
		}
		return false;
	}

	void addKnownFile(IFile file, String charsetName) {
		String propertiesString= charsetName + PROPERTIES_DELIMETER + String.valueOf(System.currentTimeMillis());
		knownFiles.setProperty(getKeyForResource(file), propertiesString);
	}

	public Object removeKnownFile(IFile file) {
		return knownFiles.remove(getKeyForResource(file));
	}

	public void removeKnownFiles(Set<IFile> files) {
		boolean hasChanged= false;
		for (IFile file : files) {
			Object removed= removeKnownFile(file);
			if (removed != null) {
				hasChanged= true;
			}
		}
		if (hasChanged) {
			recordKnownFiles();
		}
	}

	public synchronized void addCVSEntriesFile(IFile cvsEntriesSourceFile) {
		addKnownFile(cvsEntriesSourceFile, ResourceHelper.getCharsetNameForFile(cvsEntriesSourceFile));
		File cvsEntriesDestinationFile= getTrackedCVSEntriesFile(cvsEntriesSourceFile);
		try {
			ResourceHelper.ensureFileExists(cvsEntriesDestinationFile);
			ResourceHelper.writeFileContent(cvsEntriesDestinationFile, ResourceHelper.readFileContent(cvsEntriesSourceFile), false);
		} catch (IOException e) {
			Debugger.logExceptionToErrorLog(e, Messages.Recorder_CVSEntriesCopyFailure);
		}
	}

	public void moveKnownFiles(IResource movedResource, IPath destination, boolean success) {
		reorganizeKnownFiles(movedResource, destination, success, true);
	}

	public void copyKnownFiles(IResource copiedResource, IPath destination, boolean success) {
		reorganizeKnownFiles(copiedResource, destination, success, false);
	}

	public void removeKnownFilesForResource(IResource resource) {
		reorganizeKnownFiles(resource, resource.getFullPath(), false, true);
	}

	private void reorganizeKnownFiles(IResource reorganizedResource, IPath destination, boolean shouldCreateNewEntry, boolean shouldRemoveOldEntry) {
		String oldKey= getKeyForResource(reorganizedResource);
		if (reorganizedResource instanceof IFile) {
			if (knownFiles.containsKey(oldKey)) {
				reorganizeKnownFile(oldKey, getKeyForPath(destination), shouldCreateNewEntry, shouldRemoveOldEntry);
				recordKnownFiles();
			}
		} else { //IContainer
			String oldPrefix= oldKey + IPath.SEPARATOR;
			Set<Entry<Object, Object>> reorganizedEntries= getKnownFileEntriesPrefixedBy(oldPrefix);
			if (!reorganizedEntries.isEmpty()) {
				String newPrefix= getKeyForPath(destination) + IPath.SEPARATOR;
				for (Entry<Object, Object> entry : reorganizedEntries) {
					String oldEntryKey= (String)entry.getKey();
					String newEntryKey= StringHelper.replacePrefix(oldEntryKey, oldPrefix, newPrefix);
					reorganizeKnownFile(oldEntryKey, newEntryKey, shouldCreateNewEntry, shouldRemoveOldEntry);
				}
				recordKnownFiles();
			}
		}
	}

	private void reorganizeKnownFile(String oldKey, String newKey, boolean shouldCreateNewEntry, boolean shouldRemoveOldEntry) {
		if (shouldCreateNewEntry) {
			knownFiles.setProperty(newKey, knownFiles.getProperty(oldKey));
		}
		if (shouldRemoveOldEntry) {
			knownFiles.remove(oldKey); //remove the old entry after the new one is added (to reuse its value above)
		}
	}

	private Set<Entry<Object, Object>> getKnownFileEntriesPrefixedBy(String prefix) {
		Set<Entry<Object, Object>> result= new HashSet<Entry<Object, Object>>();
		for (Entry<Object, Object> entry : knownFiles.entrySet()) {
			if (((String)entry.getKey()).startsWith(prefix)) {
				result.add(entry);
			}
		}
		return result;
	}

	private String getKeyForResource(IResource resource) {
		return ResourceHelper.getPortableResourcePath(resource);
	}

	private String getKeyForPath(IPath path) {
		return path.toPortableString();
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
