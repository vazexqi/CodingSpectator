/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.move;

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
			recordMoveAndResetState();
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
		//Should not add this operation if the relation will become N-to-N.
		//TODO: Instead of breaking the grouped operations at this point, implement a heuristic that will
		//consider structural relations (e.g., belong to the same method) and/or time relation (what is closer in time).
		if (astOperation.isAdd()) {
			if (addOperationsCount > 0 && deleteOperationsCount > 1) {
				return false;
			}
		} else if (astOperation.isDelete()) {
			if (addOperationsCount > 1 && deleteOperationsCount > 0) {
				return false;
			}
		}
		return true;
	}

	public void recordMoveAndResetState() {
		if (addOperationsCount > 0 && deleteOperationsCount > 0) {
			operations.get(0).setFirstMoved(true);
			getLastOperation().setLastMoved(true);
			for (ASTOperation operation : operations) {
				operation.setMoveID(nextMoveID);
			}
			nextMoveID++;
		}
		resetState();
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
