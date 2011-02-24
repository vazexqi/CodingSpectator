/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.recording;

import edu.illinois.codingspectator.codingtracker.operations.UserOperation;
import edu.illinois.codingspectator.saferecorder.SafeRecorder;

/**
 * 
 * @author Stas Negara
 * @author Mohsen Vakilian
 * 
 */
public class TextRecorder {

	private final static SafeRecorder recorderInstance= new SafeRecorder("codingtracker/codechanges.txt");

	public static void record(UserOperation userOperation) {
		recorderInstance.record(userOperation.generateSerializationText());
	}

	public static String getMainRecordFilePath() {
		return recorderInstance.mainRecordFilePath;
	}

}
