/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

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

	private static final int NO_NODE_ID= -1;

	private static final String PRIVATE_MODIFIER= "private";

	private static final ASTOperationRecorder astOperationRecorder= ASTOperationRecorder.getInstance();

	private static Set<RefactoringProperty> properties;

	private static long activationTimestamp;

	//TODO: Maybe this map should be shrank according to the time threshold once it reaches certain size.
	private static final Map<Long, ASTOperation> addedMovedNodes= new HashMap<Long, ASTOperation>();


	/**
	 * Returns null if there is no refactoring property corresponding to the given operation.
	 * 
	 * @param operation
	 * @return
	 */
	public static Set<RefactoringProperty> retrieveProperties(ASTOperation operation) {
		activationTimestamp= operation.getTime();
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
		} else if (operation.isChange()) {
			handleChangedNode(affectedNode, operation);
		}
		return properties;
	}

	private static void handleChangedNode(ASTNode changedNode, ASTOperation operation) {
		long moveID= operation.getMoveID();
		if (moveID != NO_NODE_ID) {
			//When a changed node is moved, it is treated as being added.
			handleAddedMovedNode(changedNode, operation, moveID);
		}
		if (changedNode instanceof SimpleName) {
			handleChangedSimpleName((SimpleName)changedNode, operation);
		} else if (changedNode instanceof Modifier && operation.getNodeNewText().equals(PRIVATE_MODIFIER)) {
			handlePrivateModifier((Modifier)changedNode);
		}
	}

	private static void handleChangedSimpleName(SimpleName changedNode, ASTOperation operation) {
		String oldEntityName= changedNode.getIdentifier();
		String newEntityName= operation.getNodeNewText();
		properties.add(new CorrectiveRefactoringProperty(oldEntityName, getNodeID(changedNode), newEntityName, activationTimestamp));
		if (isDeclaredEntity(changedNode)) {
			handleChangedDeclaredEntity(changedNode, oldEntityName, newEntityName);
		} else {
			properties.add(new ChangedEntityNameInUsageRefactoringProperty(oldEntityName, newEntityName, activationTimestamp));
		}
	}

	private static void handlePrivateModifier(Modifier modifier) {
		ASTNode parent= modifier.getParent();
		//The change in visibility of a field should not be a result of adding the field itself.
		if (parent instanceof FieldDeclaration && !astOperationRecorder.isAdded(parent)) {
			for (Object fragment : ((FieldDeclaration)parent).fragments()) {
				SimpleName fieldName= ((VariableDeclarationFragment)fragment).getName();
				properties.add(MadeFieldPrivateRefactoringProperty.createInstance(fieldName.getIdentifier(), getNodeID(fieldName), activationTimestamp));
			}
		}
	}

	private static void handleChangedDeclaredEntity(SimpleName changedNode, String oldEntityName, String newEntityName) {
		if (isLocalVariableOrFieldDeclaredEntity(changedNode)) {
			if (isInLocalVariableDeclaration(changedNode)) {
				properties.add(new ChangedVariableNameInDeclarationRefactoringProperty(oldEntityName, newEntityName, activationTimestamp));
			} else if (isInFieldDeclaration(changedNode)) {
				properties.add(new ChangedFieldNameInDeclarationRefactoringProperty(oldEntityName, newEntityName, activationTimestamp));
			}
		} else if (isMethodDeclaredEntity(changedNode, true)) {
			properties.add(new ChangedMethodNameInDeclarationRefactoringProperty(oldEntityName, newEntityName, activationTimestamp));
		} else if (isTypeDeclaredEntity(changedNode)) {
			properties.add(new ChangedTypeNameInDeclarationRefactoringProperty(oldEntityName, newEntityName, activationTimestamp));
		}
	}

	private static void handleDeletedNode(ASTNode deletedNode, ASTOperation operation) {
		if (deletedNode instanceof VariableDeclarationFragment) {
			handleDeletedVariableDeclarationFragment((VariableDeclarationFragment)deletedNode);
		} else {
			long moveID= operation.getMoveID();
			if (moveID != NO_NODE_ID) {
				handleDeletedMovedNode(deletedNode, operation, moveID);
			}
			if (deletedNode instanceof SimpleName && !isDeclaredEntity(deletedNode)) {
				handleDeletedReferenceNode((SimpleName)deletedNode);
			}
		}
	}

	private static void handleDeletedVariableDeclarationFragment(VariableDeclarationFragment variableDeclaration) {
		if (isInLocalVariableDeclaration(variableDeclaration)) {
			String variableName= variableDeclaration.getName().getIdentifier();
			properties.add(new DeletedVariableDeclarationRefactoringProperty(variableName, NO_NODE_ID, activationTimestamp));
		}
	}

	private static void handleDeletedMovedNode(ASTNode deletedNode, ASTOperation operation, long moveID) {
		NodeDescriptor nodeDescriptor= new NodeDescriptor(operation);
		SimpleName entityName= getDeclaredEntityNameForInitializer(deletedNode);
		if (entityName != null && isInLocalVariableDeclaration(deletedNode)) {
			properties.add(new MovedFromVariableInitializationRefactoringProperty(nodeDescriptor, entityName.getIdentifier(), getNodeID(entityName), moveID, activationTimestamp));
		} else {
			if (operation.getMethodID() != NO_NODE_ID && !isTooSimpleForExtractMethod(deletedNode)) {
				properties.add(new MovedFromMethodRefactoringProperty(operation.getMethodID(), moveID, activationTimestamp));
			}
			long parentID= getParentID(deletedNode, true);
			if (parentID != NO_NODE_ID) {
				properties.add(new MovedFromUsageRefactoringProperty(nodeDescriptor, moveID, parentID, activationTimestamp));
			}
		}
	}

	private static void handleDeletedReferenceNode(SimpleName referenceNode) {
		long parentID= getParentID(referenceNode, true);
		if (parentID != NO_NODE_ID) {
			String entityName= referenceNode.getIdentifier();
			properties.add(new DeletedEntityReferenceRefactoringProperty(entityName, NO_NODE_ID, parentID, activationTimestamp));
		}
	}

	private static void handleAddedNode(ASTNode addedNode, ASTOperation operation) {
		if (addedNode instanceof VariableDeclarationFragment) {
			handleAddedVariableDeclarationFragment((VariableDeclarationFragment)addedNode);
		} else if (addedNode instanceof MethodDeclaration) {
			handleAddedMethodDeclaration((MethodDeclaration)addedNode, operation);
		} else {
			long moveID= operation.getMoveID();
			if (moveID != NO_NODE_ID) {
				handleAddedMovedNode(addedNode, operation, moveID);
			}
			long methodID= operation.getMethodID();
			if (methodID != NO_NODE_ID) {
				handleAddedNodeToMethod(addedNode, methodID);
			}
			if (addedNode instanceof SimpleName && !isDeclaredEntity(addedNode)) {
				handleAddedReferenceNode((SimpleName)addedNode);
			}
			if (addedNode instanceof Modifier && operation.getNodeText().equals(PRIVATE_MODIFIER)) {
				handlePrivateModifier((Modifier)addedNode);
			}
		}
	}

	private static void handleAddedVariableDeclarationFragment(VariableDeclarationFragment variableDeclaration) {
		String entityName= variableDeclaration.getName().getIdentifier();
		long entityNameNodeID= getNodeID(variableDeclaration.getName());
		if (isInLocalVariableDeclaration(variableDeclaration)) {
			properties.add(new AddedVariableDeclarationRefactoringProperty(entityName, entityNameNodeID, activationTimestamp));
		} else if (isInFieldDeclaration(variableDeclaration)) {
			properties.add(new AddedFieldDeclarationRefactoringProperty(entityName, entityNameNodeID, activationTimestamp));
		}
		handleAddedVariableInitializer(variableDeclaration, entityName, entityNameNodeID);
	}

	private static void handleAddedVariableInitializer(VariableDeclarationFragment variableDeclaration, String entityName, long entityNameNodeID) {
		Expression initializer= variableDeclaration.getInitializer();
		if (initializer != null) {
			ASTOperation addMoveOperation= addedMovedNodes.remove(getNodeID(initializer));
			if (addMoveOperation != null) {
				NodeDescriptor nodeDescriptor= new NodeDescriptor(addMoveOperation);
				long moveID= addMoveOperation.getMoveID();
				if (isInLocalVariableDeclaration(variableDeclaration)) {
					properties.add(new MovedToVariableInitializationRefactoringProperty(nodeDescriptor, entityName, entityNameNodeID, moveID, activationTimestamp));
				} else if (isInFieldDeclaration(variableDeclaration)) {
					properties.add(new MovedToFieldInitializationRefactoringProperty(nodeDescriptor, entityName, entityNameNodeID, moveID, activationTimestamp));
				}
			}
		}
	}

	private static void handleAddedMethodDeclaration(MethodDeclaration methodDeclaration, ASTOperation operation) {
		String methodName= methodDeclaration.getName().getIdentifier();
		long methodNameNodeID= getNodeID(methodDeclaration.getName());
		long methodID= operation.getNodeID();
		properties.add(new AddedMethodDeclarationRefactoringProperty(methodName, methodNameNodeID, methodID, activationTimestamp));
		//This is somewhat inefficient, but since at this point we do not know whether the added method is a getter,
		//a setter, or none of the above, we just tentatively consider every possibility.
		properties.add(new AddedGetterMethodDeclarationRefactoringProperty(methodName, methodID, activationTimestamp));
		properties.add(new AddedSetterMethodDeclarationRefactoringProperty(methodName, methodID, activationTimestamp));
	}

	private static void handleAddedNodeToMethod(ASTNode addedNode, long methodID) {
		if (addedNode instanceof MethodInvocation) {
			handleAddedMethodInvocation((MethodInvocation)addedNode, methodID);
		}
		if (addedNode instanceof ReturnStatement) {
			handleAddedReturnStatement((ReturnStatement)addedNode, methodID);
		}
		if (addedNode instanceof ExpressionStatement) {
			handleAddedExpressionStatement((ExpressionStatement)addedNode, methodID);
		}
	}

	private static void handleAddedMethodInvocation(MethodInvocation methodInvocation, long sourceMethodID) {
		String destinationMethodName= methodInvocation.getName().getIdentifier();
		long destinationMethodNameNodeID= getNodeID(methodInvocation.getName());
		String sourceMethodName= ASTHelper.getContainingMethod(methodInvocation).getName().getIdentifier();
		properties.add(new AddedMethodInvocationRefactoringProperty(destinationMethodName, destinationMethodNameNodeID, sourceMethodName, sourceMethodID, activationTimestamp));
	}

	private static void handleAddedReturnStatement(ReturnStatement returnStatement, long methodID) {
		SimpleName returnedFieldName= getFieldNameFromExpression(returnStatement.getExpression());
		if (returnedFieldName != null) {
			FieldDeclaration fieldDeclaration= getFieldDeclarationForName(returnedFieldName);
			if (fieldDeclaration != null) {
				long returnedFieldNameNodeID= getNodeID(returnedFieldName);
				properties.add(new AddedFieldReturnRefactoringProperty(returnedFieldName.getIdentifier(), returnedFieldNameNodeID, methodID, activationTimestamp));
				//The private visibility of a field should not be a result of adding the field itself.
				if (Modifier.isPrivate(fieldDeclaration.getModifiers()) && !astOperationRecorder.isAdded(fieldDeclaration)) {
					properties.add(MadeFieldPrivateRefactoringProperty.createInstance(returnedFieldName.getIdentifier(), returnedFieldNameNodeID, activationTimestamp));
				}
			}
		}
	}

	private static void handleAddedExpressionStatement(ExpressionStatement expressionStatement, long methodID) {
		Expression expression= expressionStatement.getExpression();
		if (expression instanceof Assignment) {
			SimpleName assignedFieldName= getFieldNameFromExpression(((Assignment)expression).getLeftHandSide());
			if (assignedFieldName != null) {
				FieldDeclaration fieldDeclaration= getFieldDeclarationForName(assignedFieldName);
				if (fieldDeclaration != null) {
					properties.add(new AddedFieldAssignmentRefactoringProperty(assignedFieldName.getIdentifier(), getNodeID(assignedFieldName), methodID, activationTimestamp));
				}
			}
		}
	}

	private static void handleAddedMovedNode(ASTNode addedNode, ASTOperation operation, long moveID) {
		NodeDescriptor nodeDescriptor= new NodeDescriptor(operation);
		if (!handleAddedMovedInitialization(addedNode, nodeDescriptor, moveID)) {
			addedMovedNodes.put(getNodeID(addedNode), operation);
			if (operation.getMethodID() != NO_NODE_ID && !isTooSimpleForExtractMethod(addedNode)) {
				SimpleName containingMethodName= ASTHelper.getContainingMethod(addedNode).getName();
				properties.add(new MovedToMethodRefactoringProperty(operation.getMethodID(), containingMethodName.getIdentifier(), getNodeID(containingMethodName), moveID, activationTimestamp));
			}
			long parentID= getParentID(addedNode, false);
			properties.add(new MovedToUsageRefactoringProperty(nodeDescriptor, moveID, parentID, activationTimestamp));
			if (addedNode instanceof SimpleName) {
				SimpleName referencedEntityName= (SimpleName)addedNode;
				properties.add(new AddedEntityReferenceRefactoringProperty(referencedEntityName.getIdentifier(), getNodeID(referencedEntityName), parentID, activationTimestamp));
			}
		}
	}

	/**
	 * Returns true if the given addedNode is indeed a moved initialization.
	 * 
	 * @param addedNode
	 * @param nodeDescriptor
	 * @param moveID
	 * @return
	 */
	private static boolean handleAddedMovedInitialization(ASTNode addedNode, NodeDescriptor nodeDescriptor, long moveID) {
		SimpleName declaredEntityName= getDeclaredEntityNameForInitializer(addedNode);
		if (declaredEntityName != null) {
			long declaredEntityNameNodeID= getNodeID(declaredEntityName);
			if (isInLocalVariableDeclaration(addedNode)) {
				properties.add(new MovedToVariableInitializationRefactoringProperty(nodeDescriptor, declaredEntityName.getIdentifier(), declaredEntityNameNodeID, moveID,
						activationTimestamp));
			} else if (isInFieldDeclaration(addedNode)) {
				properties.add(new MovedToFieldInitializationRefactoringProperty(nodeDescriptor, declaredEntityName.getIdentifier(), declaredEntityNameNodeID, moveID, activationTimestamp));
			}
			return true;
		}
		return false;
	}

	private static void handleAddedReferenceNode(SimpleName referenceNode) {
		properties.add(new AddedEntityReferenceRefactoringProperty(referenceNode.getIdentifier(), getNodeID(referenceNode), getParentID(referenceNode, false), activationTimestamp));
	}

	private static boolean isTooSimpleForExtractMethod(ASTNode node) {
		return node instanceof SimpleName || node instanceof SimpleType;
	}

	private static SimpleName getFieldNameFromExpression(Expression expression) {
		if (expression instanceof FieldAccess) {
			return ((FieldAccess)expression).getName();
		}
		if (expression instanceof SimpleName) {
			return (SimpleName)expression;
		}
		return null;
	}

	private static FieldDeclaration getFieldDeclarationForName(SimpleName name) {
		TypeDeclaration containingType= ASTHelper.getContainingType(name);
		if (containingType != null) {
			for (FieldDeclaration fieldDeclaration : containingType.getFields()) {
				for (Object fragment : fieldDeclaration.fragments()) {
					if (((VariableDeclarationFragment)fragment).getName().getIdentifier().equals(name.getIdentifier())) {
						return fieldDeclaration;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Returns null if the given node is not an initializer in a variable declaration fragment.
	 * 
	 * @param node
	 * @return
	 */
	private static SimpleName getDeclaredEntityNameForInitializer(ASTNode node) {
		VariableDeclarationFragment variableDeclaration= getEnclosingVariableDeclarationFragment(node);
		if (variableDeclaration != null && node == variableDeclaration.getInitializer()) {
			return variableDeclaration.getName();
		}
		return null;
	}

	private static boolean isDeclaredEntity(ASTNode node) {
		return isLocalVariableOrFieldDeclaredEntity(node) || isMethodDeclaredEntity(node, false) || isTypeDeclaredEntity(node);
	}

	private static boolean isLocalVariableOrFieldDeclaredEntity(ASTNode node) {
		VariableDeclarationFragment variableDeclaration= getEnclosingVariableDeclarationFragment(node);
		return variableDeclaration != null && node == variableDeclaration.getName();
	}

	private static boolean isMethodDeclaredEntity(ASTNode node, boolean ignoreConstructors) {
		ASTNode parent= ASTHelper.getParent(node, MethodDeclaration.class);
		if (parent != null) {
			MethodDeclaration methodDeclaration= (MethodDeclaration)parent;
			//Constructors are detected as methods without a return type. We can not
			//use isConstructor, since it would not work if the class is being renamed (which makes
			//original constructors to be considered as ordinary methods until they are renamed as well).
			return node == methodDeclaration.getName() && (!ignoreConstructors || methodDeclaration.getReturnType2() != null);
		}
		return false;
	}

	private static boolean isTypeDeclaredEntity(ASTNode node) {
		ASTNode typeDeclaration= ASTHelper.getParent(node, TypeDeclaration.class);
		return typeDeclaration != null && node == ((TypeDeclaration)typeDeclaration).getName();
	}

	private static boolean isInLocalVariableDeclaration(ASTNode node) {
		return ASTHelper.getParent(node, VariableDeclarationStatement.class) != null;
	}

	private static boolean isInFieldDeclaration(ASTNode node) {
		return ASTHelper.getParent(node, FieldDeclaration.class) != null;
	}

	private static VariableDeclarationFragment getEnclosingVariableDeclarationFragment(ASTNode node) {
		ASTNode parent= ASTHelper.getParent(node, VariableDeclarationFragment.class);
		if (parent != null) {
			return (VariableDeclarationFragment)parent;
		}
		return null;
	}

	private static long getParentID(ASTNode node, boolean isOld) {
		ASTNode parentNode= node.getParent();
		if (isOld) {
			if (astOperationRecorder.isDeleted(parentNode)) {
				return NO_NODE_ID;
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
