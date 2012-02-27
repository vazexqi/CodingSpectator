/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties;

import java.util.HashSet;
import java.util.Set;

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
	 * TODO: Refactor!!!
	 * 
	 * @param operation
	 * @return
	 */
	public static Set<RefactoringProperty> retrieveProperties(ASTOperation operation) {
		Set<RefactoringProperty> properties= new HashSet<RefactoringProperty>();
		ASTNode rootNode;
		if (operation.isAdd()) {
			rootNode= astOperationRecorder.getLastNewRootNode();
		} else {
			rootNode= astOperationRecorder.getLastOldRootNode();
		}
		ASTNode affectedNode= ASTNodesIdentifier.getASTNodeFromPositonalID(rootNode, operation.getPositionalID());
		if (operation.isAdd()) {
			long moveID= operation.getMoveID();
			long parentID= getParentID(affectedNode, false);
			if (moveID != -1) {
				ASTNode parent= ASTHelper.getParent(affectedNode, VariableDeclarationFragment.class);
				if (parent != null) {
					VariableDeclarationFragment variableDeclaration= (VariableDeclarationFragment)parent;
					if (affectedNode == variableDeclaration.getInitializer()) {
						String variableName= variableDeclaration.getName().getIdentifier();
						properties.add(new MovedToInitializationRefactoringProperty(new NodeDescriptor(operation), variableName, moveID));
					}
				} else {
					properties.add(new MovedToUsageRefactoringProperty(new NodeDescriptor(operation), moveID, parentID));
					if (affectedNode instanceof SimpleName) {
						String variableName= ((SimpleName)affectedNode).getIdentifier();
						properties.add(new AddedVariableReferenceRefactoringProperty(variableName, parentID));
					}
				}
			} else {
				if (affectedNode instanceof VariableDeclarationFragment) {
					String variableName= ((VariableDeclarationFragment)affectedNode).getName().getIdentifier();
					properties.add(new AddedVariableDeclarationRefactoringProperty(variableName));
				} else if (affectedNode instanceof SimpleName &&
							ASTHelper.getParent(affectedNode, VariableDeclarationFragment.class) == null) {
					String variableName= ((SimpleName)affectedNode).getIdentifier();
					properties.add(new AddedVariableReferenceRefactoringProperty(variableName, parentID));
				}
			}
		} else if (operation.isDelete()) {
			long moveID= operation.getMoveID();
			long parentID= getParentID(affectedNode, true);
			if (moveID != -1) {
				ASTNode parent= ASTHelper.getParent(affectedNode, VariableDeclarationFragment.class);
				if (parent != null) {
					VariableDeclarationFragment variableDeclaration= (VariableDeclarationFragment)parent;
					if (affectedNode == variableDeclaration.getInitializer()) {
						String variableName= variableDeclaration.getName().getIdentifier();
						properties.add(new MovedFromInitializationRefactoringProperty(new NodeDescriptor(operation), variableName, moveID));
					}
				} else if (parentID != -1) {
					properties.add(new MovedFromUsageRefactoringProperty(new NodeDescriptor(operation), moveID, parentID));
				}
			} else {
				if (affectedNode instanceof VariableDeclarationFragment) {
					String variableName= ((VariableDeclarationFragment)affectedNode).getName().getIdentifier();
					properties.add(new DeletedVariableDeclarationRefactoringProperty(variableName));
				} else if (parentID != -1 && affectedNode instanceof SimpleName &&
							ASTHelper.getParent(affectedNode, VariableDeclarationFragment.class) == null) {
					String variableName= ((SimpleName)affectedNode).getIdentifier();
					properties.add(new DeletedVariableReferenceRefactoringProperty(variableName, parentID));
				}
			}
		} else if (operation.isChange() && affectedNode instanceof SimpleName) {
			String oldVariableName= ((SimpleName)affectedNode).getIdentifier();
			String newVariableName= operation.getNodeNewText();
			ASTNode parent= ASTHelper.getParent(affectedNode, VariableDeclarationFragment.class);
			if (parent != null && ((VariableDeclarationFragment)parent).getName() == affectedNode) {
				properties.add(new ChangedVariableNameInDeclarationRefactoringProperty(oldVariableName, newVariableName));
			} else {
				properties.add(new ChangedVariableNameInUsageRefactoringProperty(oldVariableName, newVariableName));
			}
		}
		return properties;
	}

	private static long getParentID(ASTNode node, boolean isOld) {
		ASTNode parentNode= node.getParent();
		if (isOld) {
			if (astOperationRecorder.isDeleted(parentNode)) {
				return -1;
			} else {
				ASTNode newParentNode= astOperationRecorder.getNewMatch(parentNode);
				if (newParentNode != null) {
					return getNodeID(newParentNode);
				} else {
					throw new RuntimeException("A parent node of a deleted node is neither deleted nor matched");
				}
			}
		} else {
			return getNodeID(parentNode);
		}
	}

	private static long getNodeID(ASTNode node) {
		return ASTNodesIdentifier.getPersistentNodeID(astOperationRecorder.getCurrentRecordedFilePath(), node);
	}

}
