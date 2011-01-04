/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.logging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;

import edu.illinois.codingspectator.codingtracker.Messages;
import edu.illinois.codingspectator.codingtracker.helpers.Debugger;
import edu.illinois.codingspectator.codingtracker.helpers.LoggerHelper;

/**
 * 
 * @author Stas Negara
 * 
 */
public class TextLogger {

	private static TextLogger loggerInstance= null;

	private File currentLogFile= null;

	private final File mainLogFile;

	private static final IPath WATCHED_PATH= Platform.getStateLocation(Platform.getBundle("org.eclipse.ltk.core.refactoring"));

	private static final String LOGGER_FOLDER= "codingtracker";

	private static final IPath LOGGER_PATH= WATCHED_PATH.append(EventLogger.FEATURE_VERSION).append(LOGGER_FOLDER);

	private static final String LOGFILE_NAME= "codechanges.txt";

	private static final IPath MAIN_LOG_FILE_PATH= LOGGER_PATH.append(LOGFILE_NAME);

	static TextLogger getInstance() {
		if (loggerInstance == null) {
			loggerInstance= new TextLogger();
		}
		return loggerInstance;
	}

	private TextLogger() {
		mainLogFile= new File(MAIN_LOG_FILE_PATH.toOSString());
		mainLogFile.getParentFile().mkdirs();
		try {
			mainLogFile.createNewFile();
			currentLogFile= mainLogFile;
		} catch (IOException e) {
			LoggerHelper.logExceptionToErrorLog(e, Messages.Logger_CreateLogFileException);
		}
	}

	/**
	 * Start writing into a temporary log file
	 */
	synchronized void commitStarted() {
		Debugger.debug("START COMMIT");
		IPath tempLogFilePath= LOGGER_PATH.append("t" + System.currentTimeMillis() + ".txt");
		currentLogFile= new File(tempLogFilePath.toOSString());
		try {
			currentLogFile.createNewFile();
		} catch (IOException e) {
			LoggerHelper.logExceptionToErrorLog(e, Messages.Logger_CreateTempLogFileException);
		}
	}

	/**
	 * Switch back to the main log and append to it whatever was written in the temporary file, then
	 * erase the temporary file
	 */
	synchronized void commitCompleted() {
		Debugger.debug("END COMMIT");
		File tempFile= currentLogFile;
		currentLogFile= mainLogFile;
		String tempContent= LoggerHelper.getFileContent(tempFile);
		log(tempContent);
		tempFile.delete();
	}

	synchronized void log(CharSequence text) {
		BufferedWriter logFileWriter= null;
		try {
			Debugger.debugFileSize("Before: ", currentLogFile);
			logFileWriter= new BufferedWriter(new FileWriter(currentLogFile, true));
			logFileWriter.append(text);
			logFileWriter.flush();
			Debugger.debugFileSize("After: ", currentLogFile);
		} catch (IOException e) {
			LoggerHelper.logExceptionToErrorLog(e, Messages.Logger_AppendLogFileException);
		} finally {
			if (logFileWriter != null) {
				try {
					logFileWriter.close();
				} catch (IOException e) {
					//do nothing
				}
			}
		}
	}

}
