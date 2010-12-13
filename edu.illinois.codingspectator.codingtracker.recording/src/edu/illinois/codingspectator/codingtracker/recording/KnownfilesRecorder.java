/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.recording;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;

import edu.illinois.codingspectator.codingtracker.helpers.Debugger;
import edu.illinois.codingspectator.codingtracker.helpers.FileHelper;
import edu.illinois.codingspectator.codingtracker.helpers.Messages;

/**
 * 
 * @author Stas Negara
 * 
 */
public class KnownfilesRecorder {

	private static KnownfilesRecorder recorderInstance= null;

	private final File knownfilesFile;

	private final Properties knownfiles= new Properties(); //Is thread-safe since SE 6

	private static final long REFRESH_INTERVAL= 7 * 24 * 60 * 60 * 1000; //Refresh knownfiles every 7 days

	private static final IPath CODINGTRACKER_PATH= Platform.getStateLocation(Platform.getBundle(Activator.PLUGIN_ID));

	private static final IPath KNOWNFILES_PATH= CODINGTRACKER_PATH.append(TextRecorder.FEATURE_VERSION);

	private static final String KNOWNFILES_FILE_NAME= "knownfiles.txt";

	private static final IPath KNOWNFILES_FILE_PATH= KNOWNFILES_PATH.append(KNOWNFILES_FILE_NAME);


	static KnownfilesRecorder getInstance() {
		if (recorderInstance == null) {
			recorderInstance= new KnownfilesRecorder();
		}
		return recorderInstance;
	}

	private KnownfilesRecorder() {
		knownfilesFile= new File(KNOWNFILES_FILE_PATH.toOSString());
		knownfilesFile.getParentFile().mkdirs();
		try {
			if (knownfilesFile.exists()) {
				knownfiles.load(new FileInputStream(knownfilesFile));
				refreshKnownfiles();
			} else {
				knownfilesFile.createNewFile();
			}
		} catch (Exception e) {
			Debugger.logExceptionToErrorLog(e, Messages.Recorder_OpenKnowfilesFileException);
		}
	}

	private void refreshKnownfiles() {
		long currentTime= System.currentTimeMillis();
		Iterator<Object> keysIterator= knownfiles.keySet().iterator();
		boolean hasChanged= false;
		while (keysIterator.hasNext()) {
			Object key= keysIterator.next();
			String timestamp= knownfiles.getProperty(key.toString());
			if (currentTime - Long.valueOf(timestamp) > REFRESH_INTERVAL) {
				keysIterator.remove();
				hasChanged= true;
			}
		}
		if (hasChanged) {
			recordKnownfiles();
		}
	}

	synchronized void recordKnownfiles() {
		Debugger.debug("recordKnownfiles");
		BufferedWriter knownfilesFileWriter= null;
		try {
			knownfilesFileWriter= new BufferedWriter(new FileWriter(knownfilesFile));
			knownfiles.store(knownfilesFileWriter, null);
		} catch (IOException e) {
			Debugger.logExceptionToErrorLog(e, Messages.Recorder_WriteKnownfilesFileException);
		} finally {
			if (knownfilesFileWriter != null) {
				try {
					knownfilesFileWriter.close();
				} catch (IOException e) {
					//do nothing
				}
			}
		}
	}

	boolean isFileKnown(IFile file) {
		return knownfiles.containsKey(FileHelper.getPortableFilePath(file));
	}

	void addKnownfile(IFile file) {
		knownfiles.setProperty(FileHelper.getPortableFilePath(file), String.valueOf(System.currentTimeMillis()));
	}

	Object removeKnownfile(IFile file) {
		return knownfiles.remove(FileHelper.getPortableFilePath(file));
	}

}
