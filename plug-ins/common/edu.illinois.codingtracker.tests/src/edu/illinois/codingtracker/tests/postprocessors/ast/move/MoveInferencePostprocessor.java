/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.move;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.tests.postprocessors.ast.ASTPostprocessor;


/**
 * This class infers "move" AST operations and records them as the corresponding attributes of the
 * previously inferred "add" and "delete" operations.
 * 
 * @author Stas Negara
 * 
 */
public class MoveInferencePostprocessor extends ASTPostprocessor {

	private final Map<NodeDescriptor, NodeOperations> nodeOperationsMap= new HashMap<NodeDescriptor, NodeOperations>();


	@Override
	protected String getRecordFileName() {
		return "codechanges.txt.inferred_ast_operations";
	}

	@Override
	protected String getResultFilePostfix() {
		return ".with_move";
	}

	@Override
	protected void postprocess(List<UserOperation> userOperations) {
		for (UserOperation userOperation : userOperations) {
			if (shouldProcess(userOperation)) {
				processASTOperation((ASTOperation)userOperation);
			}
		}
		for (Entry<NodeDescriptor, NodeOperations> entry : nodeOperationsMap.entrySet()) {
			entry.getValue().markMoveAndResetState();
		}
		//Processing is done, record the updated sequence.
		for (UserOperation userOperation : userOperations) {
			record(userOperation);
		}
	}

	private void processASTOperation(ASTOperation astOperation) {
		if (astOperation.isAdd() || astOperation.isChange() || astOperation.isDelete()) {
			NodeDescriptor nodeDescriptor= new NodeDescriptor(astOperation);
			NodeOperations nodeOperations= nodeOperationsMap.get(nodeDescriptor);
			if (nodeOperations == null) {
				nodeOperationsMap.put(nodeDescriptor, new NodeOperations(astOperation));
			} else {
				nodeOperations.addOperation(astOperation);
			}
		}
	}

}
