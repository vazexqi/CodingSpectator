/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties;

import java.util.HashSet;
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

	private static final String PRIVATE_MODIFIER= "private";

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
		} else if (operation.isChange()) {
			handleChangedNode(affectedNode, operation);
		}
		return properties;
	}

	private static void handleChangedNode(ASTNode changedNode, ASTOperation operation) {
		if (changedNode instanceof SimpleName) {
			handleChangedSimpleName((SimpleName)changedNode, operation);
		} else if (changedNode instanceof Modifier && operation.getNodeNewText().equals(PRIVATE_MODIFIER)) {
			handlePrivateModifier((Modifier)changedNode);
		}
	}

	private static void handleChangedSimpleName(SimpleName changedNode, ASTOperation operation) {
		String oldEntityName= changedNode.getIdentifier();
		String newEntityName= operation.getNodeNewText();
		if (isDeclaredEntity(changedNode)) {
			handleChangedDeclaredEntity(changedNode, oldEntityName, newEntityName);
		} else {
			properties.add(new ChangedEntityNameInUsageRefactoringProperty(oldEntityName, newEntityName));
		}
	}

	private static void handlePrivateModifier(Modifier modifier) {
		ASTNode parent= modifier.getParent();
		//The change in visibility of a field should not be a result of adding the field itself.
		if (parent instanceof FieldDeclaration && !astOperationRecorder.isAdded(parent)) {
			for (Object fragment : ((FieldDeclaration)parent).fragments()) {
				String fieldName= ((VariableDeclarationFragment)fragment).getName().getIdentifier();
				properties.add(MadeFieldPrivateRefactoringProperty.createInstance(fieldName));
			}
		}
	}

	private static void handleChangedDeclaredEntity(SimpleName changedNode, String oldEntityName, String newEntityName) {
		if (isLocalVariableOrFieldDeclaredEntity(changedNode)) {
			if (isInLocalVariableDeclaration(changedNode)) {
				properties.add(new ChangedVariableNameInDeclarationRefactoringProperty(oldEntityName, newEntityName));
			} else if (isInFieldDeclaration(changedNode)) {
				properties.add(new ChangedFieldNameInDeclarationRefactoringProperty(oldEntityName, newEntityName));
			}
		} else if (isMethodDeclaredEntity(changedNode, true)) {
			properties.add(new ChangedMethodNameInDeclarationRefactoringProperty(oldEntityName, newEntityName));
		} else if (isTypeDeclaredEntity(changedNode)) {
			properties.add(new ChangedTypeNameInDeclarationRefactoringProperty(oldEntityName, newEntityName));
		}
	}

	private static void handleDeletedNode(ASTNode deletedNode, ASTOperation operation) {
		if (deletedNode instanceof VariableDeclarationFragment) {
			handleDeletedVariableDeclarationFragment((VariableDeclarationFragment)deletedNode);
		} else {
			long moveID= operation.getMoveID();
			if (moveID != -1) {
				handleDeletedMovedNode(deletedNode, operation, moveID);
			}
			if (deletedNode instanceof SimpleName && !isDeclaredEntity(deletedNode)) {
				handleDeletedReferenceNode((SimpleName)deletedNode);
			}
		}
	}

	private static void handleDeletedVariableDeclarationFragment(VariableDeclarationFragment variableDeclaration) {
		String entityName= getDeclaredEntityName(variableDeclaration);
		if (isInLocalVariableDeclaration(variableDeclaration)) {
			properties.add(new DeletedVariableDeclarationRefactoringProperty(entityName));
		}
	}

	private static void handleDeletedMovedNode(ASTNode deletedNode, ASTOperation operation, long moveID) {
		NodeDescriptor nodeDescriptor= new NodeDescriptor(operation);
		String entityName= getDeclaredEntityNameForInitializer(deletedNode);
		if (entityName != null && isInLocalVariableDeclaration(deletedNode)) {
			properties.add(new MovedFromVariableInitializationRefactoringProperty(nodeDescriptor, entityName, moveID));
		} else {
			if (operation.getMethodID() != -1 && !isTooSimpleForExtractMethod(deletedNode)) {
				properties.add(new MovedFromMethodRefactoringProperty(operation.getMethodID(), moveID));
			}
			long parentID= getParentID(deletedNode, true);
			if (parentID != -1) {
				properties.add(new MovedFromUsageRefactoringProperty(nodeDescriptor, moveID, parentID));
			}
		}
	}

	private static void handleDeletedReferenceNode(SimpleName referenceNode) {
		long parentID= getParentID(referenceNode, true);
		if (parentID != -1) {
			String entityName= referenceNode.getIdentifier();
			properties.add(new DeletedEntityReferenceRefactoringProperty(entityName, parentID));
		}
	}

	private static void handleAddedNode(ASTNode addedNode, ASTOperation operation) {
		if (addedNode instanceof VariableDeclarationFragment) {
			handleAddedVariableDeclarationFragment((VariableDeclarationFragment)addedNode);
		} else if (addedNode instanceof MethodDeclaration) {
			handleAddedMethodDeclaration((MethodDeclaration)addedNode, operation);
		} else {
			long moveID= operation.getMoveID();
			if (moveID != -1) {
				handleAddedMovedNode(addedNode, operation, moveID);
			}
			long methodID= operation.getMethodID();
			if (methodID != -1) {
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
		String entityName= getDeclaredEntityName(variableDeclaration);
		if (isInLocalVariableDeclaration(variableDeclaration)) {
			properties.add(new AddedVariableDeclarationRefactoringProperty(entityName));
		} else if (isInFieldDeclaration(variableDeclaration)) {
			properties.add(new AddedFieldDeclarationRefactoringProperty(entityName));
		}
	}

	private static void handleAddedMethodDeclaration(MethodDeclaration methodDeclaration, ASTOperation operation) {
		String methodName= methodDeclaration.getName().getIdentifier();
		long methodID= operation.getNodeID();
		properties.add(new AddedMethodDeclarationRefactoringProperty(methodName, methodID));
		//This is somewhat inefficient, but since at this point we do not know whether the added method is a getter,
		//a setter, or none of the above, we just tentatively consider every possibility.
		properties.add(new AddedGetterMethodDeclarationRefactoringProperty(methodName, methodID));
		properties.add(new AddedSetterMethodDeclarationRefactoringProperty(methodName, methodID));
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
		String sourceMethodName= ASTHelper.getContainingMethod(methodInvocation).getName().getIdentifier();
		properties.add(new AddedMethodInvocationRefactoringProperty(destinationMethodName, sourceMethodName, sourceMethodID));
	}

	private static void handleAddedReturnStatement(ReturnStatement returnStatement, long methodID) {
		SimpleName returnedFieldName= getFieldNameFromExpression(returnStatement.getExpression());
		if (returnedFieldName != null) {
			FieldDeclaration fieldDeclaration= getFieldDeclarationForName(returnedFieldName);
			if (fieldDeclaration != null) {
				properties.add(new AddedFieldReturnRefactoringProperty(returnedFieldName.getIdentifier(), methodID));
				//The private visibility of a field should not be a result of adding the field itself.
				if (Modifier.isPrivate(fieldDeclaration.getModifiers()) && !astOperationRecorder.isAdded(fieldDeclaration)) {
					properties.add(MadeFieldPrivateRefactoringProperty.createInstance(returnedFieldName.getIdentifier()));
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
					properties.add(new AddedFieldAssignmentRefactoringProperty(assignedFieldName.getIdentifier(), methodID));
				}
			}
		}
	}

	private static void handleAddedMovedNode(ASTNode addedNode, ASTOperation operation, long moveID) {
		NodeDescriptor nodeDescriptor= new NodeDescriptor(operation);
		if (!handleAddedMovedInitializationOrAssignment(addedNode, nodeDescriptor, moveID)) {
			if (operation.getMethodID() != -1 && !isTooSimpleForExtractMethod(addedNode)) {
				properties.add(new MovedToMethodRefactoringProperty(operation.getMethodID(), moveID));
			}
			long parentID= getParentID(addedNode, false);
			properties.add(new MovedToUsageRefactoringProperty(nodeDescriptor, moveID, parentID));
			if (addedNode instanceof SimpleName) {
				String referencedEntityName= ((SimpleName)addedNode).getIdentifier();
				properties.add(new AddedEntityReferenceRefactoringProperty(referencedEntityName, parentID));
			}
		}
	}

	/**
	 * Returns true if the given addedNode is indeed a moved initialization or assignment.
	 * 
	 * @param addedNode
	 * @param nodeDescriptor
	 * @param moveID
	 * @return
	 */
	private static boolean handleAddedMovedInitializationOrAssignment(ASTNode addedNode, NodeDescriptor nodeDescriptor, long moveID) {
		String declaredEntityName= getDeclaredEntityNameForInitializer(addedNode);
		if (declaredEntityName != null) {
			if (isInLocalVariableDeclaration(addedNode)) {
				properties.add(new MovedToVariableInitializationOrAssignmentRefactoringProperty(nodeDescriptor, declaredEntityName, moveID));
			} else if (isInFieldDeclaration(addedNode)) {
				properties.add(new MovedToFieldInitializationRefactoringProperty(nodeDescriptor, declaredEntityName, moveID));
			}
			return true;
		} else {
			String assignedEntityName= getAssignedEntityNameForAssignment(addedNode);
			if (assignedEntityName != null && isInExpressionStatement(addedNode)) {
				properties.add(new MovedToVariableInitializationOrAssignmentRefactoringProperty(nodeDescriptor, assignedEntityName, moveID));
				return true;
			}
		}
		return false;
	}

	private static void handleAddedReferenceNode(SimpleName referenceNode) {
		String entityName= referenceNode.getIdentifier();
		properties.add(new AddedEntityReferenceRefactoringProperty(entityName, getParentID(referenceNode, false)));
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
	private static String getDeclaredEntityNameForInitializer(ASTNode node) {
		VariableDeclarationFragment variableDeclaration= getEnclosingVariableDeclarationFragment(node);
		if (variableDeclaration != null && node == variableDeclaration.getInitializer()) {
			return getDeclaredEntityName(variableDeclaration);
		}
		return null;
	}

	/**
	 * Returns null if the given node is not an assigned expression in a variable assignment.
	 * 
	 * @param node
	 * @return
	 */
	private static String getAssignedEntityNameForAssignment(ASTNode node) {
		ASTNode parent= ASTHelper.getParent(node, Assignment.class);
		if (parent != null) {
			Assignment assignment= (Assignment)parent;
			if (node == assignment.getRightHandSide()) {
				Expression leftHandSide= assignment.getLeftHandSide();
				if (leftHandSide instanceof SimpleName) {
					return ((SimpleName)leftHandSide).getIdentifier();
				}
			}
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

	private static boolean isInExpressionStatement(ASTNode node) {
		return ASTHelper.getParent(node, ExpressionStatement.class) != null;
	}

	private static VariableDeclarationFragment getEnclosingVariableDeclarationFragment(ASTNode node) {
		ASTNode parent= ASTHelper.getParent(node, VariableDeclarationFragment.class);
		if (parent != null) {
			return (VariableDeclarationFragment)parent;
		}
		return null;
	}

	private static String getDeclaredEntityName(VariableDeclarationFragment variableDeclaration) {
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
