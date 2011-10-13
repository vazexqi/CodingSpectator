/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.recording;

import org.eclipse.jdt.core.dom.ASTNode;

import edu.illinois.codingspectator.saferecorder.SafeRecorder;
import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.ast.ASTFileOperation;
import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.operations.ast.ASTOperation.OperationKind;
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


	public static void record(UserOperation userOperation) {
		//Before any user operation, except text change operations, flush the accumulated AST changes.
		if (!(userOperation instanceof TextChangeOperation)) {
			//TODO: Some part of the below code are duplicated in TextRecorder.
			//Saving a file does not force flushing since the corresponding AST might be broken.
			astRecorder.flushCurrentTextChanges(!(userOperation instanceof SavedFileOperation));
		}
		lastTimestamp= userOperation.getTime();
		performRecording(userOperation);
	}

	public static void recordASTOperation(OperationKind operationKind, ASTNode astNode, String newNodeText, long nodeID, long methodID,
											int methodLinesCount, int methodCyclomaticComplexity, String fullMethodName) {
		ASTOperation astOperation= new ASTOperation(operationKind, astNode, newNodeText, nodeID, methodID, methodLinesCount, methodCyclomaticComplexity, fullMethodName, getASTOperationTimestamp());
		performRecording(astOperation);
	}

	public static void recordASTFileOperation(String astFilePath) {
		performRecording(new ASTFileOperation(astFilePath, getASTOperationTimestamp()));
	}

	private static void performRecording(UserOperation userOperation) {
		safeRecorder.record(userOperation.generateSerializationText());
	}

	private static long getASTOperationTimestamp() {
		if (ASTOperationRecorder.isInReplayMode) {
			return lastTimestamp;
		} else {
			return System.currentTimeMillis();
		}
	}

	public static String getMainRecordFilePath() {
		return safeRecorder.mainRecordFilePath;
	}

}
