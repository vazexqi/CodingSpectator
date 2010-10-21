/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.logging;

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

import edu.illinois.codingspectator.codingtracker.Activator;
import edu.illinois.codingspectator.codingtracker.Messages;
import edu.illinois.codingspectator.codingtracker.helpers.Debugger;
import edu.illinois.codingspectator.codingtracker.helpers.LoggerHelper;

/**
 * 
 * @author Stas Negara
 * 
 */
public class KnownfilesLogger {

	private static KnownfilesLogger loggerInstance= null;

	private final File knownfilesFile;

	private final Properties knownfiles= new Properties(); //Is thread-safe since SE 6

	private static final long REFRESH_INTERVAL= 7 * 24 * 60 * 60 * 1000; //Refresh knownfiles every 7 days

	private static final IPath CODINGTRACKER_PATH= Platform.getStateLocation(Platform.getBundle(Activator.PLUGIN_ID));

	private static final IPath KNOWNFILES_PATH= CODINGTRACKER_PATH.append(EventLogger.FEATURE_VERSION);

	private static final String KNOWNFILES_FILE_NAME= "knownfiles.txt";

	private static final IPath KNOWNFILES_FILE_PATH= KNOWNFILES_PATH.append(KNOWNFILES_FILE_NAME);


	static KnownfilesLogger getInstance() {
		if (loggerInstance == null) {
			loggerInstance= new KnownfilesLogger();
		}
		return loggerInstance;
	}

	private KnownfilesLogger() {
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
			LoggerHelper.logExceptionToErrorLog(e, Messages.Logger_OpenKnowfilesFileException);
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
			logKnownfiles();
		}
	}

	synchronized void logKnownfiles() {
		Debugger.debug("logKnownfiles");
		BufferedWriter knownfilesFileWriter= null;
		try {
			knownfilesFileWriter= new BufferedWriter(new FileWriter(knownfilesFile));
			knownfiles.store(knownfilesFileWriter, null);
		} catch (IOException e) {
			LoggerHelper.logExceptionToErrorLog(e, Messages.Logger_WriteKnownfilesFileException);
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
		return knownfiles.containsKey(LoggerHelper.getPortableFilePath(file));
	}

	void addKnownfile(IFile file) {
		knownfiles.setProperty(LoggerHelper.getPortableFilePath(file), String.valueOf(System.currentTimeMillis()));
	}

	Object removeKnownfile(IFile file) {
		return knownfiles.remove(LoggerHelper.getPortableFilePath(file));
	}

}
