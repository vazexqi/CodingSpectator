/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.saferecorder;

import java.io.File;
import java.io.IOException;

import edu.illinois.codingspectator.data.CodingSpectatorDataPlugin;
import edu.illinois.codingtracker.helpers.Debugger;
import edu.illinois.codingtracker.helpers.Messages;
import edu.illinois.codingtracker.helpers.ResourceHelper;

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
	 * Start writing into a temporary record file.
	 */
	synchronized void aboutToCommit() {
		Debugger.debug("IS ABOUT TO COMMIT");
		String tempRecordFilePath= mainRecordFilePath + "." + System.currentTimeMillis() + ".tmp";
		currentRecordFile= new File(tempRecordFilePath);
	}

	/**
	 * Switch back to the main record file and append to it whatever was written in the temporary
	 * file, then erase the temporary file.
	 */
	synchronized void commitCompleted() {
		Debugger.debug("COMMIT COMPLETED");
		//Switch back to the main record file only if the switch to a temporary record file actually happened.
		if (currentRecordFile != mainRecordFile) {
			File tempFile= currentRecordFile;
			currentRecordFile= mainRecordFile;
			//Temporary file does not exist at this point if nothing is recorded to it during data uploading
			if (tempFile.exists()) {
				String tempContent= ResourceHelper.readFileContent(tempFile);
				record(tempContent);
				tempFile.delete();
			}
		}
	}

	public synchronized void record(CharSequence text) {
		try {
			ResourceHelper.ensureFileExists(currentRecordFile);
		} catch (IOException e) {
			Debugger.logExceptionToErrorLog(e, Messages.Recorder_CreateRecordFileException + currentRecordFile.getName());
		}
		Debugger.debugFileSize("Before: ", currentRecordFile);
		try {
			ResourceHelper.writeFileContent(currentRecordFile, text, true);
		} catch (IOException e) {
			Debugger.logExceptionToErrorLog(e, Messages.Recorder_AppendRecordFileException + currentRecordFile.getName());
		}
		Debugger.debugFileSize("After: ", currentRecordFile);
	}

}
