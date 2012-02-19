/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.recording;

import edu.illinois.codingspectator.saferecorder.SafeRecorder;
import edu.illinois.codingtracker.helpers.Configuration;
import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.operations.files.EditedFileOperation;
import edu.illinois.codingtracker.operations.files.EditedUnsychronizedFileOperation;
import edu.illinois.codingtracker.operations.files.SavedFileOperation;
import edu.illinois.codingtracker.operations.files.snapshoted.NewFileOperation;
import edu.illinois.codingtracker.operations.textchanges.TextChangeOperation;
import edu.illinois.codingtracker.recording.ast.ASTOperationRecorder;

/**
 * 
 * @author Stas Negara
 * @author Mohsen Vakilian
 * 
 */
public class TextRecorder {

	private final static ASTOperationRecorder astRecorder= ASTOperationRecorder.getInstance();

	private final static SafeRecorder safeRecorder= new SafeRecorder("codingtracker/codechanges.txt");


	public static void record(UserOperation userOperation) {
		if (!Configuration.isInReplayMode) {
			//Before any user operation, excluding several exceptions, flush the accumulated AST changes.
			if (!(userOperation instanceof ASTOperation) && !(userOperation instanceof TextChangeOperation) &&
					!(userOperation instanceof EditedFileOperation) && !(userOperation instanceof EditedUnsychronizedFileOperation) &&
					!(userOperation instanceof NewFileOperation)) {
				//Saving a file does not force flushing since the corresponding AST might be broken.
				astRecorder.flushCurrentTextChanges(!(userOperation instanceof SavedFileOperation));
			}
		}
		safeRecorder.record(userOperation.generateSerializationText());
	}

	public static String getMainRecordFilePath() {
		return safeRecorder.mainRecordFilePath;
	}

}
