/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.recording;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryService;

import edu.illinois.codingspectator.codingtracker.helpers.Debugger;
import edu.illinois.codingspectator.codingtracker.helpers.FileHelper;
import edu.illinois.codingspectator.codingtracker.helpers.Messages;
import edu.illinois.codingspectator.codingtracker.operations.UserOperation;

/**
 * 
 * @author Stas Negara
 * 
 */
@SuppressWarnings("restriction")
public class TextRecorder {

	private static TextRecorder recorderInstance= null;

	private File currentRecordFile= null;

	private final File mainRecordFile;

	private static final IPath WATCHED_PATH= Platform.getStateLocation(Platform.getBundle("org.eclipse.ltk.core.refactoring"));

	static final String FEATURE_VERSION= RefactoringHistoryService.getFeatureVersion().toString();

	private static final String RECORDER_FOLDER= "codingtracker";

	private static final IPath RECORDER_PATH= WATCHED_PATH.append(FEATURE_VERSION).append(RECORDER_FOLDER);

	private static final String RECORDFILE_NAME= "codechanges.txt";

	//Made public to facilitate testing
	public static final IPath MAIN_RECORD_FILE_PATH= RECORDER_PATH.append(RECORDFILE_NAME);

	public static TextRecorder getInstance() {
		if (recorderInstance == null) {
			recorderInstance= new TextRecorder();
		}
		return recorderInstance;
	}

	private TextRecorder() {
		mainRecordFile= new File(MAIN_RECORD_FILE_PATH.toOSString());
		mainRecordFile.getParentFile().mkdirs();
		try {
			mainRecordFile.createNewFile();
			currentRecordFile= mainRecordFile;
		} catch (IOException e) {
			Debugger.logExceptionToErrorLog(e, Messages.Recorder_CreateRecordFileException);
		}
	}

	/**
	 * Start writing into a temporary record file
	 */
	synchronized void commitStarted() {
		Debugger.debug("START COMMIT");
		IPath tempRecordFilePath= RECORDER_PATH.append("t" + System.currentTimeMillis() + ".txt");
		currentRecordFile= new File(tempRecordFilePath.toOSString());
		try {
			currentRecordFile.createNewFile();
		} catch (IOException e) {
			Debugger.logExceptionToErrorLog(e, Messages.Recorder_CreateTempRecordFileException);
		}
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

	public void record(UserOperation userOperation) {
		record(userOperation.generateSerializationText());
	}

	private synchronized void record(CharSequence text) {
		BufferedWriter recordFileWriter= null;
		try {
			Debugger.debugFileSize("Before: ", currentRecordFile);
			recordFileWriter= new BufferedWriter(new FileWriter(currentRecordFile, true));
			recordFileWriter.append(text);
			recordFileWriter.flush();
			Debugger.debugFileSize("After: ", currentRecordFile);
		} catch (IOException e) {
			Debugger.logExceptionToErrorLog(e, Messages.Recorder_AppendRecordFileException);
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

}
