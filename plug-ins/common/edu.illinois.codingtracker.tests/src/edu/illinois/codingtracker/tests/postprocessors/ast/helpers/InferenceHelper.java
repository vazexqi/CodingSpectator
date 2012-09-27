/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.helpers;

import org.eclipse.jdt.core.dom.ASTNode;

import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.recording.ast.ASTOperationRecorder;
import edu.illinois.codingtracker.recording.ast.identification.ASTNodesIdentifier;



/**
 * 
 * @author Stas Negara
 * 
 */
public class InferenceHelper {

	public static ASTNode getAffectedNode(ASTOperation operation) {
		ASTNode rootNode= getRootNodeForOperation(operation);
		return ASTNodesIdentifier.getASTNodeFromPositonalID(rootNode, operation.getPositionalID());
	}

	public static ASTNode getRootNodeForOperation(ASTOperation operation) {
		ASTOperationRecorder astOperationRecorder= ASTOperationRecorder.getInstance();
		ASTNode rootNode;
		if (operation.isAdd()) {
			rootNode= astOperationRecorder.getLastNewRootNode();
		} else {
			rootNode= astOperationRecorder.getLastOldRootNode();
		}
		return rootNode;
	}

}
