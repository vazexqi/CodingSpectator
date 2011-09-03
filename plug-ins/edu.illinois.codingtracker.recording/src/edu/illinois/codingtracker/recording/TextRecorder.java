/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.recording;

import edu.illinois.codingspectator.saferecorder.SafeRecorder;
import edu.illinois.codingtracker.listeners.ast.ASTListener;
import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.operations.files.EditedFileOperation;
import edu.illinois.codingtracker.operations.files.EditedUnsychronizedFileOperation;
import edu.illinois.codingtracker.operations.textchanges.TextChangeOperation;

/**
 * 
 * @author Stas Negara
 * @author Mohsen Vakilian
 * 
 */
public class TextRecorder {

	private final static ASTListener astListener= ASTListener.getInstance();

	private final static SafeRecorder recorderInstance= new SafeRecorder("codingtracker/codechanges.txt");

	public static void record(UserOperation userOperation) {
		//Before any user operation, excluding several exceptions, flush the accumulated AST changes.
		if (!(userOperation instanceof ASTOperation) && !(userOperation instanceof TextChangeOperation) &&
				!(userOperation instanceof EditedFileOperation) && !(userOperation instanceof EditedUnsychronizedFileOperation)) {
			astListener.flushCurrentTextChange();
		}
		recorderInstance.record(userOperation.generateSerializationText());
	}

	public static String getMainRecordFilePath() {
		return recorderInstance.mainRecordFilePath;
	}

}
