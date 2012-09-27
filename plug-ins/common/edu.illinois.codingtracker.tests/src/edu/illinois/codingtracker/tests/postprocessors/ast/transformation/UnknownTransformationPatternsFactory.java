/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.transformation;

import org.eclipse.jdt.core.dom.ASTNode;

import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.recording.ast.ASTOperationRecorder;
import edu.illinois.codingtracker.recording.ast.helpers.ASTHelper;
import edu.illinois.codingtracker.tests.postprocessors.ast.helpers.InferenceHelper;



/**
 * This class creates transformation patterns corresponding to ASTOperations.
 * 
 * @author Stas Negara
 * 
 */
public class UnknownTransformationPatternsFactory {

	private static final ASTOperationRecorder astOperationRecorder= ASTOperationRecorder.getInstance();


	/**
	 * Returns null if there is no pattern corresponding to the given operation.
	 * 
	 * @param operation
	 * @return
	 */
	public static UnknownTransformationPattern retrieveTransformationPattern(ASTOperation operation) {
		if (operation.isChange()) {
			return null; //So far, only add and delete operations can contribute to patterns.
		}
		ASTNode affectedNode= InferenceHelper.getAffectedNode(operation);
		if (ASTHelper.getAllChildren(affectedNode).size() < 2) { //Note that children include the affected node as well.
			return null; //So far, only structurally non-trivial nodes contribute to patterns.
		}
		return constructTransformationPatternForNode(affectedNode);
	}

	private static UnknownTransformationPattern constructTransformationPatternForNode(ASTNode node) {
		return new UnknownTransformationPattern();
	}

}
