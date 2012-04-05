/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.recording;

import edu.illinois.codingspectator.saferecorder.SafeRecorder;
import edu.illinois.codingtracker.helpers.Configuration;
import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.ast.ASTFileOperation;
import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.operations.ast.ASTOperationDescriptor;
import edu.illinois.codingtracker.operations.ast.CompositeNodeDescriptor;
import edu.illinois.codingtracker.operations.files.SavedFileOperation;
import edu.illinois.codingtracker.operations.textchanges.TextChangeOperation;
import edu.illinois.codingtracker.recording.ast.ASTOperationRecorder;

/**
 * 
 * @author Stas Negara
 * 
 */
public class ASTInferenceTextRecorder {

	private final static ASTOperationRecorder astRecorder= ASTOperationRecorder.getInstance();

	private final static SafeRecorder safeRecorder= new SafeRecorder("codingtracker/codechanges_ast.txt");

	private static long lastTimestamp;


	/**
	 * When isSimulatedRecord is true, this method flushes the text changes, if necessary, and
	 * updates the timestamp.
	 * 
	 * @param userOperation
	 * @param isSimulatedRecord
	 */
	public static void record(UserOperation userOperation, boolean isSimulatedRecord) {
		long operationTime= userOperation.getTime();
		//Before any user operation, except text change operations, flush the accumulated AST changes.
		if (!(userOperation instanceof TextChangeOperation)) {
			//TODO: Some part of the below code are duplicated in TextRecorder.
			//Saving a file does not force flushing since the corresponding AST might be broken.
			astRecorder.flushCurrentTextChanges(!(userOperation instanceof SavedFileOperation));
		}
		lastTimestamp= operationTime;
		performRecording(userOperation, isSimulatedRecord);
	}

	public static void recordASTOperation(ASTOperationDescriptor operationDescriptor, CompositeNodeDescriptor affectedNodeDescriptor) {
		ASTOperation astOperation= new ASTOperation(operationDescriptor, affectedNodeDescriptor, getASTOperationTimestamp());
		performRecording(astOperation, false);
	}

	public static void recordASTFileOperation(String astFilePath) {
		performRecording(new ASTFileOperation(astFilePath, getASTOperationTimestamp()), false);
	}

	private static void performRecording(UserOperation userOperation, boolean isSimulatedRecord) {
		if (!isSimulatedRecord) {
			safeRecorder.record(userOperation.generateSerializationText());
		}
	}

	private static long getASTOperationTimestamp() {
		if (Configuration.isInReplayMode) {
			return lastTimestamp;
		} else {
			return System.currentTimeMillis();
		}
	}

	public static String getMainRecordFilePath() {
		return safeRecorder.mainRecordFilePath;
	}

}
