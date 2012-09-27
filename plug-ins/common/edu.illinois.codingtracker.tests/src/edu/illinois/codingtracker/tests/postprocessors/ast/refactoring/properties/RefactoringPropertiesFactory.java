/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.recording.ast.ASTOperationRecorder;
import edu.illinois.codingtracker.recording.ast.helpers.ASTHelper;
import edu.illinois.codingtracker.recording.ast.identification.ASTNodesIdentifier;
import edu.illinois.codingtracker.tests.postprocessors.ast.helpers.InferenceHelper;
import edu.illinois.codingtracker.tests.postprocessors.ast.move.NodeDescriptor;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.ReplacedEntityWithExpressionRefactoringFragment;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.ReplacedExpressionWithEntityRefactoringFragment;



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

	private static final Set<RefactoringProperty> properties= new HashSet<RefactoringProperty>();;

	private static long activationTimestamp;

	//TODO: Maybe this map should be shrank according to the time threshold once it reaches certain size.
	private static final Map<Long, List<ASTOperation>> addedMovedNodes= new HashMap<Long, List<ASTOperation>>();

	private static final Set<List<ASTOperation>> batchAddedMovedNodes= new HashSet<List<ASTOperation>>();

	private static final List<ASTOperation> batchOperations= new LinkedList<ASTOperation>();

	private static ASTNode batchNewRootNode;

	private static ASTNode batchOldRootNode;


	/**
	 * Returns null if there is no refactoring property corresponding to the given operation.
	 * 
	 * @param operation
	 * @return
	 */
	public static Set<RefactoringProperty> retrieveProperties(ASTOperation operation) {
		initializeRetrieval(operation);
		ASTNode affectedNode= InferenceHelper.getAffectedNode(operation);
		if (operation.isAdd()) {
			handleAddedNode(affectedNode, operation);
		} else if (operation.isDelete()) {
			handleDeletedNode(affectedNode, operation);
		} else if (operation.isChange()) {
			handleChangedNode(affectedNode, operation);
		}
		postProcessProperties(operation);
		postprocessAddedMovedNodes(operation);
		return properties;
	}

	private static void initializeRetrieval(ASTOperation operation) {
		if (astOperationRecorder.getLastNewRootNode() != batchNewRootNode ||
				astOperationRecorder.getLastOldRootNode() != batchOldRootNode) {
			batchOperations.clear();
			batchAddedMovedNodes.clear();
			batchNewRootNode= astOperationRecorder.getLastNewRootNode();
			batchOldRootNode= astOperationRecorder.getLastOldRootNode();
		}
		batchOperations.add(operation);
		activationTimestamp= operation.getTime();
		properties.clear();
	}

	private static void postProcessProperties(ASTOperation operation) {
		for (RefactoringProperty refactoringProperty : properties) {
			//Set the main operation before going through possibly related operations.
			refactoringProperty.setMainOperation(operation);
			for (ASTOperation batchOperation : batchOperations) {
				refactoringProperty.addPossiblyRelatedOperation(batchOperation);
			}
		}
	}

	private static void postprocessAddedMovedNodes(ASTOperation operation) {
		for (List<ASTOperation> addedMovedNodeOperations : batchAddedMovedNodes) {
			ASTOperation mainOperation= addedMovedNodeOperations.get(0);
			if (mainOperation != operation &&
					ASTHelper.getAllChildren(InferenceHelper.getAffectedNode(mainOperation)).contains(InferenceHelper.getAffectedNode(operation))) {
				addedMovedNodeOperations.add(operation);
			}
		}
	}

	private static void handleChangedNode(ASTNode changedNode, ASTOperation operation) {
		long moveID= operation.getMoveID();
		long parentID= getParentID(changedNode, false);
		if (moveID != NO_NODE_ID) {
			handleAddingMovedChangedNode(changedNode, operation, moveID, parentID);
		}
		long deletingChangeMoveID= operation.getDeletingChangeMoveID();
		if (deletingChangeMoveID != NO_NODE_ID) {
			handleDeletingMovedChangedNode(changedNode, operation, parentID, deletingChangeMoveID);
		}
		if (changedNode instanceof SimpleName) {
			handleChangedSimpleName((SimpleName)changedNode, operation);
		} else if (changedNode instanceof Modifier && operation.getNodeNewText().equals(PRIVATE_MODIFIER)) {
			handlePrivateModifier((Modifier)changedNode);
		}
	}

	private static void handleAddingMovedChangedNode(ASTNode changedNode, ASTOperation operation, long moveID, long parentID) {
		NodeDescriptor nodeDescriptor= new NodeDescriptor(operation, false);
		handleAddedMovedInitialization(changedNode, nodeDescriptor, moveID);
		handleMovedToMethodNode(changedNode, operation, moveID);
		if (changedNode instanceof SimpleName) {
			properties.add(ReplacedEntityWithExpressionRefactoringFragment.createRefactoring(
					new MovedToUsageRefactoringProperty(nodeDescriptor, moveID, parentID, activationTimestamp),
					new DeletedEntityReferenceRefactoringProperty(operation.getNodeText(), NO_NODE_ID, parentID, activationTimestamp)));
		}
	}

	private static void handleDeletingMovedChangedNode(ASTNode changedNode, ASTOperation operation, long parentID, long deletingChangeMoveID) {
		handleMovedFromMethodNode(changedNode, operation, deletingChangeMoveID);
		if (changedNode instanceof SimpleName) {
			properties.add(ReplacedExpressionWithEntityRefactoringFragment.createRefactoring(
					new MovedFromUsageRefactoringProperty(new NodeDescriptor(operation, true), deletingChangeMoveID, parentID, activationTimestamp),
					createEntityReference(operation.getNodeNewText(), changedNode, parentID)));
		}
	}

	private static RefactoringProperty createEntityReference(String entityName, ASTNode entityNode, long parentID) {
		return new AddedEntityReferenceRefactoringProperty(entityName, getNodeID(entityNode), parentID, getEnclosingClassNodeID(entityNode), activationTimestamp);
	}

	private static void handleChangedSimpleName(SimpleName changedNode, ASTOperation operation) {
		String oldEntityName= changedNode.getIdentifier();
		String newEntityName= operation.getNodeNewText();
		long methodID= operation.getMethodID();
		properties.add(new CorrectiveRefactoringProperty(oldEntityName, getNodeID(changedNode), newEntityName, activationTimestamp));
		if (isDeclaredEntity(changedNode)) {
			handleChangedDeclaredEntity(changedNode, oldEntityName, newEntityName, methodID);
		} else {
			long nodeID= getNodeID(changedNode);
			MethodInvocation methodInvocation= getNamedMethodInvocation(changedNode);
			if (methodInvocation != null) {
				handleChangedMethodNameInInvocation(oldEntityName, newEntityName, methodID, nodeID, methodInvocation);
			} else {
				long declaringMethodID= getDeclaringMethodID(changedNode, oldEntityName, newEntityName);
				if (declaringMethodID == -1) {
					properties.add(new ChangedGlobalEntityNameInUsageRefactoringProperty(oldEntityName, newEntityName, nodeID, methodID, activationTimestamp));
				} else {
					properties.add(new ChangedLocalEntityNameInUsageRefactoringProperty(oldEntityName, newEntityName, nodeID, declaringMethodID, activationTimestamp));
				}
			}
		}
		handleMethodInvocationChange(changedNode, operation);
	}

	private static void handleChangedMethodNameInInvocation(String oldEntityName, String newEntityName, long methodID, long nodeID, MethodInvocation methodInvocation) {
		int argumentsCount= methodInvocation.arguments().size();
		String oldMethodName= getPartialMethodSignature(oldEntityName, argumentsCount);
		String newMethodName= getPartialMethodSignature(newEntityName, argumentsCount);
		properties.add(new ChangedMethodNameInInvocationRefactoringProperty(oldMethodName, newMethodName, nodeID, methodID, activationTimestamp));
	}

	private static void handleMethodInvocationChange(SimpleName changedNode, ASTOperation operation) {
		//First, need to get the new node.
		ASTNode matchingNode= astOperationRecorder.getNewMatch(changedNode);
		if (!(matchingNode instanceof SimpleName)) {
			throw new RuntimeException("Could not find a matching node for a changed simple name.");
		}
		SimpleName newChangedNode= (SimpleName)matchingNode;
		MethodInvocation methodInvocation= getNamedMethodInvocation(newChangedNode);
		if (methodInvocation != null) {
			long methodID= operation.getMethodID();
			if (methodID != NO_NODE_ID) {
				//Changing the name of a method invocation is basically adding a new method invocation.
				handleAddedMethodInvocation(methodInvocation, methodID, false);
			}
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

	private static void handleChangedDeclaredEntity(SimpleName changedNode, String oldEntityName, String newEntityName, long methodID) {
		if (isLocalVariableOrFieldDeclaredEntity(changedNode)) {
			if (isInVariableDeclarationStatement(changedNode) || isInSingleVariableDeclaration(changedNode)) {
				long enclosingMethodNodeID= getEnclosingMethodNodeID(changedNode);
				if (enclosingMethodNodeID == -1) {
					enclosingMethodNodeID= methodID;
				}
				properties.add(new ChangedVariableNameInDeclarationRefactoringProperty(oldEntityName, newEntityName, enclosingMethodNodeID, activationTimestamp));
			} else if (isInFieldDeclaration(changedNode)) {
				properties.add(new ChangedFieldNameInDeclarationRefactoringProperty(oldEntityName, newEntityName, activationTimestamp));
			}
		} else {
			MethodDeclaration methodDeclaration= getMethodDeclaredEntity(changedNode, true);
			if (methodDeclaration != null) {
				handleChangedMethodNameInDeclaration(oldEntityName, newEntityName, methodDeclaration);
			} else if (getMethodDeclaredEntity(changedNode, false) != null) {
				properties.add(new ChangedTypeNameInConstructorRefactoringProperty(oldEntityName, newEntityName, getNodeID(changedNode), activationTimestamp));
			} else if (isTypeDeclaredEntity(changedNode)) {
				properties.add(new ChangedTypeNameInDeclarationRefactoringProperty(oldEntityName, newEntityName, activationTimestamp));
			}
		}
	}

	private static void handleChangedMethodNameInDeclaration(String oldEntityName, String newEntityName, MethodDeclaration methodDeclaration) {
		//TODO: Consider the signature of the method more fully than just the number of parameters, if needed.
		int formalParametersCount= methodDeclaration.parameters().size();
		//TODO: Find a more general way to handle VARARG. Note that currently, if a VARARG method is invoked with different
		//number of parameters, then renaming this method will produce a separate Rename Method refactoring for each number of parameters used.
		//Simulate VARARG as up to 5 additional parameters. 
		int maxParametersCount= methodDeclaration.isVarargs() ? formalParametersCount + 5 : formalParametersCount;
		//VARARG also means that there might be no parameter whatsoever.
		int startParametersCount= methodDeclaration.isVarargs() ? formalParametersCount - 1 : formalParametersCount;
		for (int parametersCount= startParametersCount; parametersCount <= maxParametersCount; parametersCount++) {
			String oldMethodName= getPartialMethodSignature(oldEntityName, parametersCount);
			String newMethodName= getPartialMethodSignature(newEntityName, parametersCount);
			properties.add(new ChangedMethodNameInDeclarationRefactoringProperty(oldMethodName, newMethodName, activationTimestamp));
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
		if (isInVariableDeclarationStatement(variableDeclaration)) {
			String variableName= variableDeclaration.getName().getIdentifier();
			properties.add(new DeletedVariableDeclarationRefactoringProperty(variableName, NO_NODE_ID, getEnclosingClassNodeID(variableDeclaration), activationTimestamp));
		}
	}

	private static void handleDeletedMovedNode(ASTNode deletedNode, ASTOperation operation, long moveID) {
		NodeDescriptor nodeDescriptor= new NodeDescriptor(operation, false);
		SimpleName entityName= getDeclaredEntityNameForInitializer(deletedNode);
		if (entityName != null && isInVariableDeclarationStatement(deletedNode)) {
			properties.add(new MovedFromVariableInitializationRefactoringProperty(nodeDescriptor, entityName.getIdentifier(), getNodeID(entityName), moveID, activationTimestamp));
		}
		handleMovedFromMethodNode(deletedNode, operation, moveID);
		long parentID= getParentID(deletedNode, true);
		if (parentID != NO_NODE_ID) {
			properties.add(new MovedFromUsageRefactoringProperty(nodeDescriptor, moveID, parentID, activationTimestamp));
		} else {
			//Extracting a parenthesized expression usually gets rid of the parentheses, and thus, 
			//the parenthesized expression is replaced with the variable reference in the usage, while the initialization
			//of the extracted variable is stripped of the parentheses.
			ASTNode parent= deletedNode.getParent();
			if (parent instanceof ParenthesizedExpression) {
				parentID= getParentID(parent, true);
				if (parentID != NO_NODE_ID) {
					properties.add(new MovedFromUsageRefactoringProperty(nodeDescriptor, moveID, parentID, activationTimestamp));
				}
			}
		}
	}

	private static void handleMovedFromMethodNode(ASTNode movedNode, ASTOperation operation, long moveID) {
		if (operation.getMethodID() != NO_NODE_ID && !isTooSimpleForExtractMethod(movedNode)) {
			properties.add(new MovedFromMethodRefactoringProperty(operation.getMethodID(), moveID, activationTimestamp));
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
				properties.add(createEntityReference(((SimpleName)addedNode).getIdentifier(), addedNode, getParentID(addedNode, false)));
			}
			if (addedNode instanceof Modifier && operation.getNodeText().equals(PRIVATE_MODIFIER)) {
				handlePrivateModifier((Modifier)addedNode);
			}
		}
	}

	private static void handleAddedVariableDeclarationFragment(VariableDeclarationFragment variableDeclaration) {
		String entityName= variableDeclaration.getName().getIdentifier();
		long entityNameNodeID= getNodeID(variableDeclaration.getName());
		if (isInVariableDeclarationStatement(variableDeclaration)) {
			properties.add(new AddedVariableDeclarationRefactoringProperty(entityName, entityNameNodeID, activationTimestamp));
		} else if (isInFieldDeclaration(variableDeclaration)) {
			properties.add(new AddedFieldDeclarationRefactoringProperty(entityName, entityNameNodeID, getEnclosingClassNodeID(variableDeclaration), activationTimestamp));
		}
		handleAddedVariableInitializer(variableDeclaration, entityName, entityNameNodeID);
	}

	private static void handleAddedVariableInitializer(VariableDeclarationFragment variableDeclaration, String entityName, long entityNameNodeID) {
		Expression initializer= variableDeclaration.getInitializer();
		if (initializer != null) {
			List<ASTOperation> addMoveOperations= addedMovedNodes.remove(getNodeID(initializer));
			if (addMoveOperations != null) {
				//Move the main operation from the beginning to the end of the list.
				ASTOperation mainAddMoveOperation= addMoveOperations.remove(0);
				addMoveOperations.add(mainAddMoveOperation);

				NodeDescriptor nodeDescriptor= new NodeDescriptor(mainAddMoveOperation, false);
				long moveID= mainAddMoveOperation.getMoveID();
				AtomicRefactoringProperty newRefactoringProperty= null;
				if (isInVariableDeclarationStatement(variableDeclaration)) {
					newRefactoringProperty= new MovedToVariableInitializationRefactoringProperty(nodeDescriptor, entityName, entityNameNodeID, moveID, activationTimestamp);
				} else if (isInFieldDeclaration(variableDeclaration)) {
					newRefactoringProperty= createMovedToFieldInitializationRefactoringProperty(variableDeclaration, nodeDescriptor, entityName, entityNameNodeID, moveID);
				}
				if (newRefactoringProperty != null) {
					newRefactoringProperty.addRelatedOperations(addMoveOperations);
					properties.add(newRefactoringProperty);
				}
			}
		}
	}

	private static MovedToFieldInitializationRefactoringProperty createMovedToFieldInitializationRefactoringProperty(ASTNode movedNode, NodeDescriptor nodeDescriptor, String entityName,
																														long entityNameNodeID, long moveID) {
		return new MovedToFieldInitializationRefactoringProperty(nodeDescriptor, entityName, entityNameNodeID, moveID, getEnclosingClassNodeID(movedNode), activationTimestamp);
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
			handleAddedMethodInvocation((MethodInvocation)addedNode, methodID, true);
		}
		if (addedNode instanceof ReturnStatement) {
			handleAddedReturnStatement((ReturnStatement)addedNode, methodID);
		}
		if (addedNode instanceof ExpressionStatement) {
			handleAddedExpressionStatement((ExpressionStatement)addedNode, methodID);
		}
	}

	private static void handleAddedMethodInvocation(MethodInvocation methodInvocation, long sourceMethodID, boolean canBeGetterOrSetter) {
		String destinationMethodName= methodInvocation.getName().getIdentifier();
		long destinationMethodNameNodeID= getNodeID(methodInvocation.getName());
		long parentID= getParentID(methodInvocation, false);
		String sourceMethodName= ASTHelper.getContainingMethod(methodInvocation).getName().getIdentifier();

		properties.add(new AddedMethodInvocationRefactoringProperty(destinationMethodName, destinationMethodNameNodeID, sourceMethodName, sourceMethodID, activationTimestamp));

		if (canBeGetterOrSetter) {
			//This is somewhat inefficient, but since at this point we do not know whether the added method is a getter,
			//a setter, or none of the above, we just tentatively consider every possibility.
			properties.add(new AddedGetterMethodInvocationRefactoringProperty(destinationMethodName, parentID, activationTimestamp));
			properties.add(new AddedSetterMethodInvocationRefactoringProperty(destinationMethodName, parentID, activationTimestamp));
		}
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
		NodeDescriptor nodeDescriptor= new NodeDescriptor(operation, false);
		long parentID= getParentID(addedNode, false);
		//Moved to usage property should be added even if the node is moved to a variable initialization
		//to support the scenario, in which a variable is inlined into the initialization of another variable.
		properties.add(new MovedToUsageRefactoringProperty(nodeDescriptor, moveID, parentID, activationTimestamp));
		if (!handleAddedMovedInitialization(addedNode, nodeDescriptor, moveID)) {
			addNewEntryToAddedMovedNodes(addedNode, operation);
			handleMovedToMethodNode(addedNode, operation, moveID);
			if (addedNode instanceof SimpleName) {
				SimpleName referencedEntityName= (SimpleName)addedNode;
				properties.add(createEntityReference(referencedEntityName.getIdentifier(), referencedEntityName, parentID));
			}
			//Inlining an expression might add a cast to it, which goes together with parentheses around the expression.
			ASTNode parent= addedNode.getParent();
			if (parent instanceof CastExpression && astOperationRecorder.isAdded(parent)) {
				addedNode= parent;
			}
			//Inlining an expression might result in adding parentheses around it, and thus, 
			//the variable reference is replaced with the parenthesized initialization expression in the usage.
			parent= addedNode.getParent();
			if (parent instanceof ParenthesizedExpression && astOperationRecorder.isAdded(parent)) {
				parentID= getParentID(parent, false);
				properties.add(new MovedToUsageRefactoringProperty(nodeDescriptor, moveID, parentID, activationTimestamp));
			}
		}
	}

	private static void handleMovedToMethodNode(ASTNode movedNode, ASTOperation operation, long moveID) {
		if (operation.getMethodID() != NO_NODE_ID && !isTooSimpleForExtractMethod(movedNode)) {
			SimpleName containingMethodName= ASTHelper.getContainingMethod(movedNode).getName();
			properties.add(new MovedToMethodRefactoringProperty(operation.getMethodID(), containingMethodName.getIdentifier(), getNodeID(containingMethodName), moveID, activationTimestamp));
		}
	}

	private static void addNewEntryToAddedMovedNodes(ASTNode addedNode, ASTOperation operation) {
		List<ASTOperation> addedMovedNodeOperations= new LinkedList<ASTOperation>();
		addedMovedNodeOperations.add(operation);
		addedMovedNodes.put(getNodeID(addedNode), addedMovedNodeOperations);
		batchAddedMovedNodes.add(addedMovedNodeOperations);
		Set<ASTNode> allAddedChildren= ASTHelper.getAllChildren(addedNode);
		for (ASTOperation batchOperation : batchOperations) {
			if (operation != batchOperation && allAddedChildren.contains(InferenceHelper.getAffectedNode(batchOperation))) {
				addedMovedNodeOperations.add(batchOperation);
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
			if (isInVariableDeclarationStatement(addedNode)) {
				properties.add(new MovedToVariableInitializationRefactoringProperty(nodeDescriptor, declaredEntityName.getIdentifier(), declaredEntityNameNodeID, moveID,
						activationTimestamp));
			} else if (isInFieldDeclaration(addedNode)) {
				properties.add(createMovedToFieldInitializationRefactoringProperty(declaredEntityName, nodeDescriptor, declaredEntityName.getIdentifier(), declaredEntityNameNodeID, moveID));
			}
			return true;
		}
		return false;
	}

	private static boolean isTooSimpleForExtractMethod(ASTNode node) {
		return node instanceof SimpleName && getNamedMethodInvocation((SimpleName)node) == null || node instanceof SimpleType ||
				node instanceof Modifier || node instanceof EmptyStatement || node instanceof CharacterLiteral ||
				node instanceof BooleanLiteral || node instanceof NullLiteral || isTooSimpleReturnForExtractMethod(node);
	}

	private static boolean isTooSimpleReturnForExtractMethod(ASTNode node) {
		//Ignore two default return statements generated by Eclipse for a newly created method.
		return node instanceof ReturnStatement && (node.toString().startsWith("return null;") ||
				node.toString().startsWith("return false;"));
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
		VariableDeclaration variableDeclaration= getEnclosingVariableDeclaration(node);
		if (variableDeclaration != null && node == variableDeclaration.getInitializer()) {
			return variableDeclaration.getName();
		}
		return null;
	}

	private static boolean isDeclaredEntity(ASTNode node) {
		return isLocalVariableOrFieldDeclaredEntity(node) || getMethodDeclaredEntity(node, false) != null || isTypeDeclaredEntity(node);
	}

	private static boolean isLocalVariableOrFieldDeclaredEntity(ASTNode node) {
		VariableDeclaration variableDeclaration= getEnclosingVariableDeclaration(node);
		return variableDeclaration != null && node == variableDeclaration.getName();
	}

	private static MethodDeclaration getMethodDeclaredEntity(ASTNode node, boolean ignoreConstructors) {
		ASTNode parent= ASTHelper.getParent(node, MethodDeclaration.class);
		if (parent instanceof MethodDeclaration) {
			MethodDeclaration methodDeclaration= (MethodDeclaration)parent;
			//Constructors are detected as methods without a return type. We can not
			//use isConstructor, since it would not work if the class is being renamed (which makes
			//original constructors to be considered as ordinary methods until they are renamed as well).
			if (node == methodDeclaration.getName() && (!ignoreConstructors || methodDeclaration.getReturnType2() != null)) {
				return methodDeclaration;
			}
		}
		return null;
	}

	private static boolean isTypeDeclaredEntity(ASTNode node) {
		ASTNode typeDeclaration= ASTHelper.getParent(node, TypeDeclaration.class);
		if (typeDeclaration != null && node == ((TypeDeclaration)typeDeclaration).getName()) {
			return true;
		}
		//enum is a kind of type.
		ASTNode enumDeclaration= ASTHelper.getParent(node, EnumDeclaration.class);
		return enumDeclaration != null && node == ((EnumDeclaration)enumDeclaration).getName();
	}

	private static boolean isInVariableDeclarationStatement(ASTNode node) {
		return ASTHelper.getParent(node, VariableDeclarationStatement.class) != null;
	}

	private static boolean isInSingleVariableDeclaration(ASTNode node) {
		return ASTHelper.getParent(node, SingleVariableDeclaration.class) != null;
	}

	private static boolean isInFieldDeclaration(ASTNode node) {
		return ASTHelper.getParent(node, FieldDeclaration.class) != null;
	}

	private static MethodInvocation getNamedMethodInvocation(SimpleName simpleName) {
		ASTNode parentNode= ASTHelper.getParent(simpleName, MethodInvocation.class);
		if (parentNode instanceof MethodInvocation) {
			MethodInvocation methodInvocation= (MethodInvocation)parentNode;
			if (methodInvocation.getName() == simpleName) {
				return methodInvocation;
			}
		}
		return null;
	}

	private static String getPartialMethodSignature(String methodName, int argumentsCount) {
		return methodName + "(" + argumentsCount + ")";
	}

	private static VariableDeclaration getEnclosingVariableDeclaration(ASTNode node) {
		ASTNode parent= ASTHelper.getParent(node, VariableDeclaration.class);
		if (parent != null) {
			return (VariableDeclaration)parent;
		}
		return null;
	}

	private static long getEnclosingClassNodeID(ASTNode node) {
		TypeDeclaration typeDeclaration= ASTHelper.getContainingType(node);
		if (typeDeclaration != null) {
			return getNodeID(typeDeclaration);
		}
		return -1;
	}

	private static long getEnclosingMethodNodeID(ASTNode node) {
		ASTNode methodDeclaration= ASTHelper.getParent(node, MethodDeclaration.class);
		if (methodDeclaration != null) {
			return getNodeID(methodDeclaration);
		}
		return -1;
	}

	private static long getParentID(ASTNode node, boolean isOld) {
		ASTNode parentNode= node.getParent();
		if (isOld) {
			if (astOperationRecorder.isDeleted(parentNode)) {
				//Account for scenarios, in which InfixExpression is removed inside other InfixExpression without removing all
				//its children.
				if (!(node instanceof SimpleName) && parentNode instanceof InfixExpression) {
					InfixExpression infixExpression= (InfixExpression)parentNode;
					if (infixExpression.getLeftOperand() == node &&
							!astOperationRecorder.isDeleted(infixExpression.getRightOperand())) {
						parentNode= infixExpression.getParent();
						if (parentNode instanceof InfixExpression && !astOperationRecorder.isDeleted(parentNode)) {
							return getNodeID(parentNode);
						}
					}
				}
				//Account for scenarios, in which FieldAccess is replaced with QulifiedName or vice-versa.
				if (parentNode instanceof FieldAccess || parentNode instanceof QualifiedName) {
					return findMatchingParentID((Expression)parentNode);
				}
				//Account for deletion of field assignments as part of the replacement with the setter method.
				if (parentNode instanceof Assignment && ((Assignment)parentNode).getLeftHandSide() == node) {
					return getParentID(parentNode, isOld);
				}
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

	public static long getNodeID(ASTNode node) {
		return ASTNodesIdentifier.getPersistentNodeID(astOperationRecorder.getCurrentRecordedFilePath(), node);
	}

	/**
	 * If a code change replaces something like 'getData().x' with 'myVar.x', e.g., as part of an
	 * Extract Temp refactoring, or vice-versa as part of an Inline Temp refactoring, then the
	 * parent's node type is changed from FieldAccess to QualifiedName, or vice-versa. As a result,
	 * the parent node is not matched and two AST node operations are generated for this parent
	 * node: one deletes/adds FieldAccess and another one adds/deletes QualifiedName. But it is
	 * important to match this parent to correctly infer an Extract Temp or an Inline Temp
	 * refactorings.
	 * 
	 * @param expression
	 * @return
	 */
	private static long findMatchingParentID(Expression expression) {
		if (!(expression instanceof FieldAccess) && !(expression instanceof QualifiedName)) {
			throw new RuntimeException("Matching parent ID could be found only for FieldAccess or QualifiedName");
		}
		ASTNode parentNode= expression.getParent();
		if (parentNode != null && !astOperationRecorder.isDeleted(parentNode)) {
			ASTNode matchingParentNode= astOperationRecorder.getNewMatch(parentNode);
			if (matchingParentNode == null) {
				throw new RuntimeException("A parent node of a deleted node is neither deleted nor matched");
			}
			for (ASTNode childNode : ASTHelper.getAllChildren(matchingParentNode)) {
				if (childNode.getParent() == matchingParentNode && astOperationRecorder.isAdded(childNode) &&
						doesMatchField(childNode, expression)) {
					return getNodeID(childNode);
				}
			}
		}
		return NO_NODE_ID;
	}

	private static boolean doesMatchField(ASTNode childNode, Expression fieldAccessOrQualifiedName) {
		if (fieldAccessOrQualifiedName instanceof FieldAccess) {
			String fieldName= ((FieldAccess)fieldAccessOrQualifiedName).getName().getIdentifier();
			return childNode instanceof QualifiedName &&
					((QualifiedName)childNode).getName().getIdentifier().equals(fieldName);
		} else {
			String fieldName= ((QualifiedName)fieldAccessOrQualifiedName).getName().getIdentifier();
			return childNode instanceof FieldAccess &&
					((FieldAccess)childNode).getName().getIdentifier().equals(fieldName);
		}
	}

	/**
	 * Returns -1 if simpleName is a global entity.
	 * 
	 * @param simpleName
	 * @param oldEntityName
	 * @param newEntityName
	 * @return
	 */
	private static long getDeclaringMethodID(SimpleName simpleName, String oldEntityName, String newEntityName) {
		//First, consider field accesses like this.<field_name>
		ASTNode parentNode= ASTHelper.getParent(simpleName, FieldAccess.class);
		if (parentNode instanceof FieldAccess) {
			if (((FieldAccess)parentNode).getName() == simpleName) {
				return -1;
			}
		}
		//Next, consider static field accesses like <class_name>.<field_name>
		parentNode= ASTHelper.getParent(simpleName, QualifiedName.class);
		if (parentNode instanceof QualifiedName) {
			if (((QualifiedName)parentNode).getName() == simpleName) {
				return -1;
			}
		}
		//Finally, look for local vs. field variable declarations.
		long declaringMethodID= getDeclaringMethodID(simpleName, oldEntityName);
		if (declaringMethodID != -1) {
			return declaringMethodID;
		}
		//TODO: Note that this heuristic will fail if the field declaration has been already updated with the new name.
		if (getFieldDeclarationForName(simpleName) != null) {
			return -1;
		}
		return getDeclaringMethodID(simpleName, newEntityName);
	}

	private static long getDeclaringMethodID(SimpleName simpleName, final String entityName) {
		ASTNode parent= ASTHelper.getParent(simpleName, MethodDeclaration.class);
		if (parent instanceof MethodDeclaration) {
			if (isDeclaredInMethod((MethodDeclaration)parent, entityName)) {
				return getNodeID(parent);
			}
			//Look one level up to account for scenarios with anonymous inner classes declared in methods.
			parent= ASTHelper.getParent(parent.getParent(), MethodDeclaration.class);
			if (parent instanceof MethodDeclaration && isDeclaredInMethod((MethodDeclaration)parent, entityName)) {
				return getNodeID(parent);
			}
		}
		return -1;
	}

	private static boolean isDeclaredInMethod(MethodDeclaration methodDeclaration, final String entityName) {
		final String foundMessage= "Found variable declaration";
		//TODO: Consider scoping rules in this search, i.e., a field might be accessed in one scope, while a local
		//variable with the same name might be declared in another scope.
		try {
			methodDeclaration.accept(new ASTVisitor() {
				@Override
				public boolean visit(SingleVariableDeclaration singleVariableDeclaration) {
					String declaredName= singleVariableDeclaration.getName().getIdentifier();
					if (declaredName.equals(entityName)) {
						//Stop the visitor.
						throw new RuntimeException(foundMessage);
					}
					return false;
				}

				@Override
				public boolean visit(VariableDeclarationFragment variableDeclarationFragment) {
					String declaredName= variableDeclarationFragment.getName().getIdentifier();
					if (declaredName.equals(entityName)) {
						//Stop the visitor.
						throw new RuntimeException(foundMessage);
					}
					return false;
				}
			});
		} catch (RuntimeException e) {
			if (e.getMessage().equals(foundMessage)) {
				return true;
			}
		}
		return false;
	}

}
