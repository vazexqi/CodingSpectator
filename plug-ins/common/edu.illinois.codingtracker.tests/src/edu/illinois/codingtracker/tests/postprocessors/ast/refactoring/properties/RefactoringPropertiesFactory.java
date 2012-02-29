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

	private static Set<RefactoringProperty> properties;


	/**
	 * Returns null if there is no refactoring property corresponding to the given operation.
	 * 
	 * @param operation
	 * @return
	 */
	public static Set<RefactoringProperty> retrieveProperties(ASTOperation operation) {
		properties= new HashSet<RefactoringProperty>();
		ASTNode rootNode;
		if (operation.isAdd()) {
			rootNode= astOperationRecorder.getLastNewRootNode();
		} else {
			rootNode= astOperationRecorder.getLastOldRootNode();
		}
		ASTNode affectedNode= ASTNodesIdentifier.getASTNodeFromPositonalID(rootNode, operation.getPositionalID());
		if (operation.isAdd()) {
			handleAddedNode(affectedNode, operation);
		} else if (operation.isDelete()) {
			handleDeletedNode(affectedNode, operation);
		} else if (operation.isChange() && affectedNode instanceof SimpleName) {
			handleChangedNode((SimpleName)affectedNode, operation);
		}
		return properties;
	}

	private static void handleChangedNode(SimpleName changedNode, ASTOperation operation) {
		String oldVariableName= changedNode.getIdentifier();
		String newVariableName= operation.getNodeNewText();
		if (isDeclaredVariable(changedNode)) {
			properties.add(new ChangedVariableNameInDeclarationRefactoringProperty(oldVariableName, newVariableName));
		} else {
			properties.add(new ChangedVariableNameInUsageRefactoringProperty(oldVariableName, newVariableName));
		}
	}

	private static void handleDeletedNode(ASTNode deletedNode, ASTOperation operation) {
		long moveID= operation.getMoveID();
		if (moveID != -1) {
			handleDeletedMovedNode(deletedNode, new NodeDescriptor(operation), moveID);
		} else {
			handleDeletedNotMovedNode(deletedNode);
		}
	}

	private static void handleDeletedMovedNode(ASTNode deletedNode, NodeDescriptor nodeDescriptor, long moveID) {
		String variableName= getDeclaredVariableNameForInitializer(deletedNode);
		if (variableName != null) {
			properties.add(new MovedFromInitializationRefactoringProperty(nodeDescriptor, variableName, moveID));
		} else {
			long parentID= getParentID(deletedNode, true);
			if (parentID != -1) {
				properties.add(new MovedFromUsageRefactoringProperty(nodeDescriptor, moveID, parentID));
			}
		}
	}

	private static void handleDeletedNotMovedNode(ASTNode deletedNode) {
		if (deletedNode instanceof VariableDeclarationFragment) {
			String variableName= getDeclaredVariableName((VariableDeclarationFragment)deletedNode);
			properties.add(new DeletedVariableDeclarationRefactoringProperty(variableName));
		} else if (deletedNode instanceof SimpleName && !isDeclaredVariable(deletedNode)) {
			long parentID= getParentID(deletedNode, true);
			if (parentID != -1) {
				String variableName= ((SimpleName)deletedNode).getIdentifier();
				properties.add(new DeletedVariableReferenceRefactoringProperty(variableName, parentID));
			}
		}
	}

	private static void handleAddedNode(ASTNode addedNode, ASTOperation operation) {
		long moveID= operation.getMoveID();
		if (moveID != -1) {
			handleAddedMovedNode(addedNode, new NodeDescriptor(operation), moveID);
		} else {
			handleAddedNotMovedNode(addedNode);
		}
	}

	private static void handleAddedMovedNode(ASTNode addedNode, NodeDescriptor nodeDescriptor, long moveID) {
		String declarationVariableName= getDeclaredVariableNameForInitializer(addedNode);
		if (declarationVariableName != null) {
			properties.add(new MovedToInitializationRefactoringProperty(nodeDescriptor, declarationVariableName, moveID));
		} else {
			long parentID= getParentID(addedNode, false);
			properties.add(new MovedToUsageRefactoringProperty(nodeDescriptor, moveID, parentID));
			if (addedNode instanceof SimpleName) {
				String referenceVariableName= ((SimpleName)addedNode).getIdentifier();
				properties.add(new AddedVariableReferenceRefactoringProperty(referenceVariableName, parentID));
			}
		}
	}

	private static void handleAddedNotMovedNode(ASTNode addedNode) {
		if (addedNode instanceof VariableDeclarationFragment) {
			String variableName= getDeclaredVariableName((VariableDeclarationFragment)addedNode);
			properties.add(new AddedVariableDeclarationRefactoringProperty(variableName));
		} else if (addedNode instanceof SimpleName && !isDeclaredVariable(addedNode)) {
			String variableName= ((SimpleName)addedNode).getIdentifier();
			properties.add(new AddedVariableReferenceRefactoringProperty(variableName, getParentID(addedNode, false)));
		}
	}

	/**
	 * Returns null if the given node is not an initializer in a variable declaration fragment.
	 * 
	 * @param node
	 * @return
	 */
	private static String getDeclaredVariableNameForInitializer(ASTNode node) {
		VariableDeclarationFragment variableDeclaration= getEnclosingVariableDeclarationFragment(node);
		if (variableDeclaration != null && node == variableDeclaration.getInitializer()) {
			return getDeclaredVariableName(variableDeclaration);
		}
		return null;
	}

	private static boolean isDeclaredVariable(ASTNode node) {
		VariableDeclarationFragment variableDeclaration= getEnclosingVariableDeclarationFragment(node);
		if (variableDeclaration != null && node == variableDeclaration.getName()) {
			return true;
		}
		return false;
	}

	private static VariableDeclarationFragment getEnclosingVariableDeclarationFragment(ASTNode node) {
		ASTNode parent= ASTHelper.getParent(node, VariableDeclarationFragment.class);
		if (parent != null) {
			return (VariableDeclarationFragment)parent;
		}
		return null;
	}

	private static String getDeclaredVariableName(VariableDeclarationFragment variableDeclaration) {
		return variableDeclaration.getName().getIdentifier();
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
