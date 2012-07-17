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
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NullLiteral;
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

	private static final Set<AtomicRefactoringProperty> properties= new HashSet<AtomicRefactoringProperty>();;

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
	public static Set<AtomicRefactoringProperty> retrieveProperties(ASTOperation operation) {
		initializeRetrieval(operation);
		ASTNode affectedNode= getAffectedNode(operation);
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

	public static ASTNode getAffectedNode(ASTOperation operation) {
		ASTNode rootNode= getRootNodeForOperation(operation);
		return ASTNodesIdentifier.getASTNodeFromPositonalID(rootNode, operation.getPositionalID());
	}

	public static ASTNode getRootNodeForOperation(ASTOperation operation) {
		ASTNode rootNode;
		if (operation.isAdd()) {
			rootNode= astOperationRecorder.getLastNewRootNode();
		} else {
			rootNode= astOperationRecorder.getLastOldRootNode();
		}
		return rootNode;
	}

	private static void postProcessProperties(ASTOperation operation) {
		for (AtomicRefactoringProperty refactoringProperty : properties) {
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
					ASTHelper.getAllChildren(getAffectedNode(mainOperation)).contains(getAffectedNode(operation))) {
				addedMovedNodeOperations.add(operation);
			}
		}
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
			String methodName= getContainingMethodName(changedNode);
			properties.add(new ChangedEntityNameInUsageRefactoringProperty(oldEntityName, newEntityName, methodName, activationTimestamp));
		}
		handleMethodInvocationChange(changedNode, operation);
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

	private static void handleChangedDeclaredEntity(SimpleName changedNode, String oldEntityName, String newEntityName) {
		if (isLocalVariableOrFieldDeclaredEntity(changedNode)) {
			if (isInVariableDeclarationStatement(changedNode) || isInSingleVariableDeclaration(changedNode)) {
				String methodName= getContainingMethodName(changedNode);
				properties.add(new ChangedVariableNameInDeclarationRefactoringProperty(oldEntityName, newEntityName, methodName, activationTimestamp));
			} else if (isInFieldDeclaration(changedNode)) {
				properties.add(new ChangedFieldNameInDeclarationRefactoringProperty(oldEntityName, newEntityName, activationTimestamp));
			}
		} else if (isMethodDeclaredEntity(changedNode, true)) {
			properties.add(new ChangedMethodNameInDeclarationRefactoringProperty(oldEntityName, newEntityName, activationTimestamp));
		} else if (isMethodDeclaredEntity(changedNode, false)) {
			properties.add(new ChangedTypeNameInConstructorRefactoringProperty(oldEntityName, newEntityName, activationTimestamp));
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
		if (isInVariableDeclarationStatement(variableDeclaration)) {
			String variableName= variableDeclaration.getName().getIdentifier();
			properties.add(new DeletedVariableDeclarationRefactoringProperty(variableName, NO_NODE_ID, getEnclosingClassNodeID(variableDeclaration), activationTimestamp));
		}
	}

	private static void handleDeletedMovedNode(ASTNode deletedNode, ASTOperation operation, long moveID) {
		NodeDescriptor nodeDescriptor= new NodeDescriptor(operation);
		SimpleName entityName= getDeclaredEntityNameForInitializer(deletedNode);
		if (entityName != null && isInVariableDeclarationStatement(deletedNode)) {
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

				NodeDescriptor nodeDescriptor= new NodeDescriptor(mainAddMoveOperation);
				long moveID= mainAddMoveOperation.getMoveID();
				AtomicRefactoringProperty newRefactoringProperty= null;
				if (isInVariableDeclarationStatement(variableDeclaration)) {
					newRefactoringProperty= new MovedToVariableInitializationRefactoringProperty(nodeDescriptor, entityName, entityNameNodeID, moveID, activationTimestamp);
				} else if (isInFieldDeclaration(variableDeclaration)) {
					newRefactoringProperty= new MovedToFieldInitializationRefactoringProperty(nodeDescriptor, entityName, entityNameNodeID, moveID, activationTimestamp);
				}
				if (newRefactoringProperty != null) {
					newRefactoringProperty.addRelatedOperations(addMoveOperations);
					properties.add(newRefactoringProperty);
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
		NodeDescriptor nodeDescriptor= new NodeDescriptor(operation);
		if (!handleAddedMovedInitialization(addedNode, nodeDescriptor, moveID)) {
			addNewEntryToAddedMovedNodes(addedNode, operation);
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

	private static void addNewEntryToAddedMovedNodes(ASTNode addedNode, ASTOperation operation) {
		List<ASTOperation> addedMovedNodeOperations= new LinkedList<ASTOperation>();
		addedMovedNodeOperations.add(operation);
		addedMovedNodes.put(getNodeID(addedNode), addedMovedNodeOperations);
		batchAddedMovedNodes.add(addedMovedNodeOperations);
		Set<ASTNode> allAddedChildren= ASTHelper.getAllChildren(addedNode);
		for (ASTOperation batchOperation : batchOperations) {
			if (operation != batchOperation && allAddedChildren.contains(getAffectedNode(batchOperation))) {
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
		return node instanceof SimpleName || node instanceof SimpleType || node instanceof Modifier ||
				node instanceof CharacterLiteral || node instanceof BooleanLiteral || node instanceof NullLiteral ||
				isTooSimpleReturnForExtractMethod(node);
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
		return isLocalVariableOrFieldDeclaredEntity(node) || isMethodDeclaredEntity(node, false) || isTypeDeclaredEntity(node);
	}

	private static boolean isLocalVariableOrFieldDeclaredEntity(ASTNode node) {
		VariableDeclaration variableDeclaration= getEnclosingVariableDeclaration(node);
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

	private static long getParentID(ASTNode node, boolean isOld) {
		ASTNode parentNode= node.getParent();
		if (isOld) {
			if (astOperationRecorder.isDeleted(parentNode)) {
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

	private static String getContainingMethodName(ASTNode node) {
		MethodDeclaration containingMethod= ASTHelper.getContainingMethod(node);
		String methodName= null;
		if (containingMethod != null) {
			methodName= containingMethod.getName().getIdentifier();
		}
		return methodName;
	}

}
