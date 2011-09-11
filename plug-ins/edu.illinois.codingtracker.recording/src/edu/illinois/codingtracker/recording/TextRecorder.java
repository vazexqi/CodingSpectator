/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.recording;

import edu.illinois.codingspectator.saferecorder.SafeRecorder;
import edu.illinois.codingtracker.operations.UserOperation;

/**
 * 
 * @author Stas Negara
 * @author Mohsen Vakilian
 * 
 */
public class TextRecorder {

	//private final static ASTOperationRecorder astRecorder= ASTOperationRecorder.getInstance();

	private final static SafeRecorder safeRecorder= new SafeRecorder("codingtracker/codechanges.txt");


	public static void record(UserOperation userOperation) {
//		//Before any user operation, excluding several exceptions, flush the accumulated AST changes.
//		if (!(userOperation instanceof ASTOperation) && !(userOperation instanceof TextChangeOperation) &&
//				!(userOperation instanceof EditedFileOperation) && !(userOperation instanceof EditedUnsychronizedFileOperation) &&
//				!(userOperation instanceof NewFileOperation)) {
//			astRecorder.flushCurrentTextChange();
//		}
		safeRecorder.record(userOperation.generateSerializationText());
	}

	public static String getMainRecordFilePath() {
		return safeRecorder.mainRecordFilePath;
	}

}
