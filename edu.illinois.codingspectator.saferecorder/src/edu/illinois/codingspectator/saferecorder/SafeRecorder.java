/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.saferecorder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import edu.illinois.codingspectator.codingtracker.helpers.Debugger;
import edu.illinois.codingspectator.codingtracker.helpers.FileHelper;
import edu.illinois.codingspectator.codingtracker.helpers.Messages;
import edu.illinois.codingspectator.data.CodingSpectatorDataPlugin;

/**
 * 
 * @author Stas Negara
 * @author Mohsen Vakilian
 * 
 */
public class SafeRecorder {

	private File currentRecordFile= null;

	private final File mainRecordFile;

	public final String mainRecordFilePath;

	public SafeRecorder(String relativePathToMainRecordFile) {
		mainRecordFilePath= CodingSpectatorDataPlugin.getVersionedStorageLocation().append(relativePathToMainRecordFile).toOSString();
		mainRecordFile= new File(mainRecordFilePath);
		currentRecordFile= mainRecordFile;
		RecorderSubmitterListener.addSafeRecorderInstance(this);
	}

	/**
	 * Start writing into a temporary record file
	 */
	synchronized void commitStarted() {
		Debugger.debug("START COMMIT");
		String tempRecordFilePath= mainRecordFilePath + "." + System.currentTimeMillis() + ".tmp";
		currentRecordFile= new File(tempRecordFilePath);
	}

	/**
	 * Switch back to the main record file and append to it whatever was written in the temporary
	 * file, then erase the temporary file
	 */
	synchronized void commitCompleted() {
		Debugger.debug("END COMMIT");
		File tempFile= currentRecordFile;
		currentRecordFile= mainRecordFile;
		String tempContent= FileHelper.getFileContent(tempFile);
		record(tempContent);
		tempFile.delete();
	}

	public synchronized void record(CharSequence text) {
		ensureFileExists(currentRecordFile);
		BufferedWriter recordFileWriter= null;
		try {
			Debugger.debugFileSize("Before: ", currentRecordFile);
			recordFileWriter= new BufferedWriter(new FileWriter(currentRecordFile, true));
			recordFileWriter.append(text);
			recordFileWriter.flush();
			Debugger.debugFileSize("After: ", currentRecordFile);
		} catch (IOException e) {
			Debugger.logExceptionToErrorLog(e, Messages.Recorder_AppendRecordFileException + currentRecordFile.getName());
		} finally {
			if (recordFileWriter != null) {
				try {
					recordFileWriter.close();
				} catch (IOException e) {
					//do nothing
				}
			}
		}
	}

	private void ensureFileExists(File file) {
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			try {
				file.createNewFile();
			} catch (IOException e) {
				Debugger.logExceptionToErrorLog(e, Messages.Recorder_CreateRecordFileException + file.getName());
			}
		}
	}

}
