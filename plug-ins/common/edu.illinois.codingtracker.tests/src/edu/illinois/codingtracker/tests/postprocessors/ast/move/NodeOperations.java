/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.move;

import java.util.LinkedList;
import java.util.List;

import edu.illinois.codingtracker.operations.ast.ASTOperation;



/**
 * 
 * @author Stas Negara
 * 
 */
public class NodeOperations {

	private static long nextMoveID= 1;

	private final long timeThreshold= 5 * 60 * 1000; // 5 minutes

	private final List<ASTOperation> operations= new LinkedList<ASTOperation>();

	private int addOperationsCount= 0;

	private int deleteOperationsCount= 0;


	public NodeOperations(ASTOperation astOperation) {
		addOperation(astOperation);
	}

	public void addOperation(ASTOperation astOperation) {
		if (!shouldAddOperation(astOperation)) {
			markMoveAndResetState();
		}
		operations.add(astOperation);
		incrementCounters(astOperation);
	}

	private boolean shouldAddOperation(ASTOperation astOperation) {
		if (operations.size() == 0) {
			return true; //A single operation should always be added.
		}
		if (Math.abs(getLastOperation().getTime() - astOperation.getTime()) > timeThreshold) {
			return false;
		}
		//First heuristic - split by the method to which the operations' nodes belong.
		//TODO: Consider other heuristics, e.g., time relation (what is closer in time).
		if (shouldSplitByMethod(astOperation)) {
			return false;
		}
		//Should not add more than one operation on the same node 
		//and should not add an operation that will make the relation N-to-N.
		if (isExistingNode(astOperation) || willBecomeNtoN(astOperation)) {
			return false;
		}
		return true;
	}

	private boolean willBecomeNtoN(ASTOperation astOperation) {
		if (astOperation.isAdd()) {
			if (addOperationsCount > 0 && deleteOperationsCount > 1) {
				return true;
			}
		} else if (astOperation.isDelete()) {
			if (addOperationsCount > 1 && deleteOperationsCount > 0) {
				return true;
			}
		}
		return false;
	}

	private boolean isExistingNode(ASTOperation astOperation) {
		for (ASTOperation addedOperation : operations) {
			if (addedOperation.getNodeID() == astOperation.getNodeID()) {
				return true;
			}
		}
		return false;
	}

	public boolean shouldSplitByMethod(ASTOperation astOperation) {
		long currentMethodID= operations.get(0).getMethodID();
		for (ASTOperation addedOperation : operations) {
			long methodID= addedOperation.getMethodID();
			//If the added operations belong to different methods or there is an operation that does not belong to any method,
			//then there is no reason to split for a new operation.
			if (methodID == -1 || methodID != currentMethodID) {
				return false;
			}
		}
		long newMethodID= astOperation.getMethodID();
		//Also, check if the already added operations form a completed move in order to avoid erroneously splitting moves that 
		//are part of extract method refactoring, i.e., when statements are moved from one method to another.
		if (isCompletedMove() && newMethodID != -1 && newMethodID != currentMethodID) {
			return true;
		}
		return false;
	}

	public void markMoveAndResetState() {
		if (isCompletedMove()) {
			operations.get(0).setFirstMoved(true);
			getLastOperation().setLastMoved(true);
			for (ASTOperation operation : operations) {
				operation.setMoveID(nextMoveID);
			}
			nextMoveID++;
		}
		resetState();
	}

	private boolean isCompletedMove() {
		return addOperationsCount > 0 && deleteOperationsCount > 0;
	}

	private void resetState() {
		operations.clear();
		addOperationsCount= 0;
		deleteOperationsCount= 0;
	}

	private void incrementCounters(ASTOperation astOperation) {
		if (astOperation.isAdd()) {
			addOperationsCount++;
		} else if (astOperation.isDelete()) {
			deleteOperationsCount++;
		} else {
			throw new RuntimeException("Can add only 'add' and 'delete' operations: " + astOperation);
		}
	}

	private ASTOperation getLastOperation() {
		int operationsCount= operations.size();
		if (operationsCount > 0) {
			return operations.get(operationsCount - 1);
		} else {
			return null;
		}
	}

}
