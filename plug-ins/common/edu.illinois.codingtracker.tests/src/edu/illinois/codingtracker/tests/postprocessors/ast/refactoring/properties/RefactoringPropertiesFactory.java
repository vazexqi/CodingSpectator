/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.recording.ast.ASTOperationRecorder;
import edu.illinois.codingtracker.recording.ast.helpers.ASTHelper;
import edu.illinois.codingtracker.recording.ast.identification.ASTNodesIdentifier;
import edu.illinois.codingtracker.tests.postprocessors.ast.move.NodeDescriptor;



/**
 * This class creates distinct properties corresponding to ASTOperations.
 * 
 * @author Stas Negara
 * 
 */
public class RefactoringPropertiesFactory {


	private static final ASTOperationRecorder astOperationRecorder= ASTOperationRecorder.getInstance();


	/**
	 * Returns null if there is no refactoring property corresponding to the given operation.
	 * 
	 * @param operation
	 * @return
	 */
	public static RefactoringProperty createProperty(ASTOperation operation) {
		ASTNode rootNode;
		if (operation.isDelete()) {
			rootNode= astOperationRecorder.getLastOldRootNode();
		} else {
			rootNode= astOperationRecorder.getLastNewRootNode();
		}
		ASTNode affectedNode= ASTNodesIdentifier.getASTNodeFromPositonalID(rootNode, operation.getPositionalID());
		if (operation.isAdd()) {
			long moveID= operation.getMoveID();
			if (moveID != -1) {
				ASTNode parent= ASTHelper.getParent(affectedNode, VariableDeclarationFragment.class);
				if (parent != null) {
					VariableDeclarationFragment variableDeclaration= (VariableDeclarationFragment)parent;
					if (affectedNode == variableDeclaration.getInitializer()) {
						String variableName= variableDeclaration.getName().getIdentifier();
						return new MovedToInitializationRefactoringProperty(new NodeDescriptor(operation), variableName, moveID);
					}
				} else if (affectedNode instanceof SimpleName) { //TODO: Duplicated code.
					String variableName= ((SimpleName)affectedNode).getIdentifier();
					return new AddedVariableReferenceRefactoringProperty(variableName, getParentID(affectedNode));
				}
			} else {
				if (affectedNode instanceof VariableDeclarationFragment) {
					String variableName= ((VariableDeclarationFragment)affectedNode).getName().getIdentifier();
					return new DeclaredVariableRefactoringProperty(variableName);
				} else if (affectedNode instanceof SimpleName &&
							ASTHelper.getParent(affectedNode, VariableDeclarationFragment.class) == null) {
					String variableName= ((SimpleName)affectedNode).getIdentifier();
					return new AddedVariableReferenceRefactoringProperty(variableName, getParentID(affectedNode));
				}
			}
		} else if (operation.isDelete()) {
			long moveID= operation.getMoveID();
			if (moveID != -1) {
				return new MovedFromUsageRefactoringProperty(new NodeDescriptor(operation), moveID, getParentID(affectedNode));
			}
		}
		return null;
	}

	private static long getParentID(ASTNode node) {
		return ASTNodesIdentifier.getPersistentNodeID(astOperationRecorder.getCurrentRecordedFilePath(), node.getParent());
	}

}
