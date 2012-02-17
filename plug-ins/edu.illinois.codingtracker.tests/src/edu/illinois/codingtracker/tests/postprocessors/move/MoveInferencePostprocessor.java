/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.move;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.illinois.codingtracker.helpers.ResourceHelper;
import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.recording.TextRecorder;
import edu.illinois.codingtracker.tests.postprocessors.CodingTrackerPostprocessor;


/**
 * This class infers "move" AST operations and records them as the corresponding attributes of the
 * previously inferred "add" and "delete" operations.
 * 
 * @author Stas Negara
 * 
 */
public class MoveInferencePostprocessor extends CodingTrackerPostprocessor {

	private final Map<NodeDescriptor, NodeOperations> nodeOperationsMap= new HashMap<NodeDescriptor, NodeOperations>();


	@Override
	protected void checkPostprocessingPreconditions() {
		//no preconditions
	}

	@Override
	protected boolean shouldPostprocessVersionFolder(String folderName) {
		return true;
	}

	@Override
	protected String getRecordFileName() {
		return "codechanges.txt.inferred_ast_operations";
	}

	@Override
	protected void postprocess(List<UserOperation> userOperations) {
		for (UserOperation userOperation : userOperations) {
			if (userOperation instanceof ASTOperation) {
				processASTOperation((ASTOperation)userOperation);
			}
		}
		for (Entry<NodeDescriptor, NodeOperations> entry : nodeOperationsMap.entrySet()) {
			entry.getValue().recordMoveAndResetState();
		}
		//Processing is done, record the updated sequence.
		for (UserOperation userOperation : userOperations) {
			TextRecorder.record(userOperation);
		}
	}

	private void processASTOperation(ASTOperation astOperation) {
		if (astOperation.isAdd() || astOperation.isDelete()) {
			NodeDescriptor nodeDescriptor= new NodeDescriptor(astOperation);
			NodeOperations nodeOperations= nodeOperationsMap.get(nodeDescriptor);
			if (nodeOperations == null) {
				nodeOperationsMap.put(nodeDescriptor, new NodeOperations(astOperation));
			} else {
				nodeOperations.addOperation(astOperation);
			}
		}
	}

	@Override
	protected String getResultFilePostfix() {
		return ".with_move";
	}

	@Override
	protected String getResult() {
		return ResourceHelper.readFileContent(mainRecordFile);
	}

}
