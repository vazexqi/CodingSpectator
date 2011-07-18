/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.saferecorder;

import java.io.File;
import java.io.FilenameFilter;
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

	private static final String TMP_EXTENSION= ".tmp";

	private File currentRecordFile= null;

	private final File mainRecordFile;

	private final File mainWatchedRecordFile;

	public final String mainRecordFilePath;

	public SafeRecorder(String relativePathToMainRecordFile) {
		mainRecordFilePath= CodingSpectatorDataPlugin.getVersionedStorageLocation().append(relativePathToMainRecordFile).toOSString();
		mainRecordFile= new File(mainRecordFilePath);
		mainWatchedRecordFile= new File(CodingSpectatorDataPlugin.getVersionedWatchedLocation().append(relativePathToMainRecordFile).toOSString());
		currentRecordFile= mainRecordFile;
		RecorderSubmitterListener.addSafeRecorderInstance(this);
	}

	/**
	 * Start writing into a temporary record file, and copy storage files to watched files.
	 */
	void aboutToCommit() {
		synchronized (this) {
			Debugger.debug("IS ABOUT TO COMMIT");
			String tempRecordFilePath= mainRecordFilePath + "." + System.currentTimeMillis() + TMP_EXTENSION;
			currentRecordFile= new File(tempRecordFilePath);
		}
		//Moving files should not be synchronized, because it is a long running operation. And it is safe,
		//because the recorder is switched to the temporary file.
		moveRecordFiles();
	}

	private void moveRecordFiles() {
		moveFileContent(mainRecordFile, mainWatchedRecordFile, true);
		final File mainRecordFolder= mainRecordFile.getParentFile();
		if (mainRecordFolder != null && mainRecordFolder.exists()) {
			String[] tempFilesToCopy= mainRecordFolder.list(new FilenameFilter() {
				@Override
				public boolean accept(File containingFolder, String fileName) {
					return shouldMoveTemporaryFileToWatchedFolder(fileName);
				}
			});
			if (tempFilesToCopy != null) {
				for (String tempFileToCopyName : tempFilesToCopy) {
					File tempFileToCopy= new File(mainRecordFolder, tempFileToCopyName);
					File destinationTempFile= new File(mainWatchedRecordFile.getParentFile(), tempFileToCopyName);
					moveFileContent(tempFileToCopy, destinationTempFile, false);
				}
			}
		}
		moveCompleted();
	}

	private boolean shouldMoveTemporaryFileToWatchedFolder(String fileName) {
		if (!fileName.equals(currentRecordFile.getName())) { //do not move the current temporary file
			//Move if this is a temporary file created for the main record file.
			return fileName.startsWith(mainRecordFile.getName()) && fileName.endsWith(TMP_EXTENSION);
		}
		return false;
	}

	private void moveFileContent(File sourceFile, File destinationFile, boolean append) {
		if (sourceFile.exists()) {
			String movedContent= ResourceHelper.readFileContent(sourceFile);
			if (ResourceHelper.isReadCompletely(sourceFile, movedContent)) {
				boolean isRecorderSuccessfully= record(movedContent, destinationFile, append);
				if (isRecorderSuccessfully) {
					//Delete source file only if both reading and recording are successful.
					sourceFile.delete();
				}
			}
		}
	}

	/**
	 * Switch back to the main record file and append to it whatever was written in the temporary
	 * file, then erase the temporary file.
	 */
	synchronized void moveCompleted() {
		Debugger.debug("MOVE COMPLETED");
		File tempFile= currentRecordFile;
		currentRecordFile= mainRecordFile;
		moveFileContent(tempFile, currentRecordFile, true);
	}

	public synchronized void record(CharSequence text) {
		record(text, currentRecordFile, true);
	}

	/**
	 * Returns whether the recording completed successfully or not.
	 * 
	 * @param text
	 * @param destinationFile
	 * @param append
	 * @return
	 */
	private boolean record(CharSequence text, File destinationFile, boolean append) {
		try {
			ResourceHelper.ensureFileExists(destinationFile);
		} catch (IOException e) {
			Debugger.logExceptionToErrorLog(e, Messages.Recorder_CreateRecordFileException + destinationFile.getName());
			return false;
		}
		Debugger.debugFileSize("Before: ", destinationFile);
		try {
			ResourceHelper.writeFileContent(destinationFile, text, append);
		} catch (IOException e) {
			Debugger.logExceptionToErrorLog(e, Messages.Recorder_AppendRecordFileException + destinationFile.getName());
			return false;
		}
		Debugger.debugFileSize("After: ", destinationFile);
		return true;
	}

}
