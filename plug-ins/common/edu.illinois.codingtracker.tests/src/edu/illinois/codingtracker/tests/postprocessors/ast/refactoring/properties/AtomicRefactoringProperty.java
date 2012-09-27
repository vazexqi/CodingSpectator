/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.recording.ast.helpers.ASTHelper;
import edu.illinois.codingtracker.recording.ast.identification.ASTNodesIdentifier;
import edu.illinois.codingtracker.recording.ast.identification.IdentifiedNodeInfo;
import edu.illinois.codingtracker.tests.postprocessors.ast.helpers.InferenceHelper;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.EncapsulateFieldRefactoring;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.ExtractConstantRefactoring;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.ExtractMethodRefactoring;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.ExtractVariableRefactoring;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.InferredRefactoring;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.InferredRefactoringFactory;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.InlineVariableRefactoring;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.RenameClassRefactoring;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.RenameFieldRefactoring;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.RenameMethodRefactoring;



/**
 * 
 * @author Stas Negara
 * 
 */
public abstract class AtomicRefactoringProperty implements RefactoringProperty {

	//Attributes, whose values should be different to allow the properties to match.
	private static final Map<String, String> disjointAttributes= new HashMap<String, String>();

	static {
		//Disjoint attributes are (obviously) symmetrical.
		disjointAttributes.put(RefactoringPropertyAttributes.SOURCE_METHOD_ID, RefactoringPropertyAttributes.DESTINATION_METHOD_ID);
		disjointAttributes.put(RefactoringPropertyAttributes.DESTINATION_METHOD_ID, RefactoringPropertyAttributes.SOURCE_METHOD_ID);
		disjointAttributes.put(RefactoringPropertyAttributes.GETTER_METHOD_ID, RefactoringPropertyAttributes.SETTER_METHOD_ID);
		disjointAttributes.put(RefactoringPropertyAttributes.SETTER_METHOD_ID, RefactoringPropertyAttributes.GETTER_METHOD_ID);
	}

	private final Map<String, Object> attributes= new HashMap<String, Object>();

	private final Set<InferredRefactoring> refactorings= new HashSet<InferredRefactoring>();

	private boolean isActive= true;

	private long activationTimestamp;

	private ASTNode mainRootNode;

	private ASTNode mainNode;

	private long mainNodeID;

	private ASTOperation mainOperation;

	private final List<ASTOperation> relatedOperations= new LinkedList<ASTOperation>();


	public AtomicRefactoringProperty(long activationTimestamp) {
		this.activationTimestamp= activationTimestamp;
	}

	@Override
	public void setMainOperation(ASTOperation mainOperation) {
		this.mainOperation= mainOperation;
		mainNode= InferenceHelper.getAffectedNode(mainOperation);
		mainNodeID= RefactoringPropertiesFactory.getNodeID(mainNode);
		mainRootNode= InferenceHelper.getRootNodeForOperation(mainOperation);
	}

	@Override
	public ASTOperation getLastRelatedOperation() {
		int relatedOperationsCount= relatedOperations.size();
		if (relatedOperationsCount == 0) {
			return null;
		}
		return relatedOperations.get(relatedOperationsCount - 1);
	}

	public void addRelatedOperations(List<ASTOperation> additionalRelatedOperatons) {
		relatedOperations.addAll(additionalRelatedOperatons);
	}

	@Override
	public boolean addPossiblyRelatedOperation(ASTOperation operation) {
		if (isRelatedOperation(operation)) {
			relatedOperations.add(operation);
			updateActivationTimestamp(operation.getTime());
			for (InferredRefactoring refactoring : refactorings) {
				refactoring.setLastContributingOperation(operation);
			}
			return true;
		}
		return false;
	}

	private boolean isRelatedOperation(ASTOperation operation) {
		if (mainOperation.getOperationKind() == operation.getOperationKind()) {
			ASTNode affectedNode= InferenceHelper.getAffectedNode(operation);
			if (mainRootNode == InferenceHelper.getRootNodeForOperation(operation)) {
				if (shouldLookFromParent()) {
					return isRelatedToVariableDeclarationFragment(affectedNode);
				}
				return ASTHelper.getAllChildren(mainNode).contains(affectedNode);
			}
			if (affectedNode instanceof Modifier) {
				return isRelatedModifier(affectedNode, operation);
			}
			if (affectedNode instanceof ReturnStatement) {
				return isRelatedReturnStatement(affectedNode, operation);
			}
			return isRelatedType(affectedNode, operation);
		}
		return false;
	}

	private boolean isRelatedType(ASTNode affectedNode, ASTOperation operation) {
		if (this instanceof AddedMethodDeclarationRefactoringProperty) {
			//A return type could be added later, so find the matching node by its persistent ID.
			ASTNode currentMainNode= getCurrentMainNodeDuringOperation(operation);
			if (currentMainNode instanceof MethodDeclaration) { //Basically, this just checks that the node is not null.
				Object property= ((MethodDeclaration)currentMainNode).getStructuralProperty(MethodDeclaration.RETURN_TYPE2_PROPERTY);
				if (property instanceof Type) { //Checks that the property is not null.
					return ASTHelper.getAllChildren((Type)property).contains(affectedNode);
				}
			}
		}
		return false;
	}

	private boolean isRelatedReturnStatement(ASTNode affectedNode, ASTOperation operation) {
		if (this instanceof AddedMethodDeclarationRefactoringProperty) {
			//A ReturnStatement could be added later, so find the matching node by its persistent ID.
			ASTNode currentMainNode= getCurrentMainNodeDuringOperation(operation);
			if (currentMainNode != null) {
				return ASTHelper.getAllChildren(currentMainNode).contains(affectedNode);
			}
		}
		return false;
	}

	private boolean isRelatedModifier(ASTNode affectedNode, ASTOperation operation) {
		if (shouldLookFromParent() || this instanceof AddedMethodDeclarationRefactoringProperty) {
			//A Modifier could be added later, so find the matching node by its persistent ID.
			ASTNode currentMainNode= getCurrentMainNodeDuringOperation(operation);
			if (currentMainNode != null) {
				ASTNode lookupNode= shouldLookFromParent() ? currentMainNode.getParent() : currentMainNode;
				return isAffectedModifier(affectedNode, lookupNode);
			}
		}
		return false;
	}

	@SuppressWarnings("rawtypes")
	private boolean isAffectedModifier(ASTNode affectedNode, ASTNode lookupNode) {
		Object property= null;
		if (lookupNode instanceof MethodDeclaration) {
			property= ((MethodDeclaration)lookupNode).getStructuralProperty(MethodDeclaration.MODIFIERS2_PROPERTY);
		} else if (lookupNode instanceof VariableDeclarationStatement) {
			property= ((VariableDeclarationStatement)lookupNode).getStructuralProperty(VariableDeclarationStatement.MODIFIERS2_PROPERTY);
		} else if (lookupNode instanceof FieldDeclaration) {
			property= ((FieldDeclaration)lookupNode).getStructuralProperty(FieldDeclaration.MODIFIERS2_PROPERTY);
		}
		if (property instanceof List) { //Checks that the property is not null.
			return ((List)property).contains(affectedNode);
		}
		return false;
	}

	private ASTNode getCurrentMainNodeDuringOperation(ASTOperation operation) {
		IdentifiedNodeInfo mainNodeInfo= ASTNodesIdentifier.getIdentifiedNodeInfo(mainNodeID);
		if (mainNodeInfo != null) {
			String positionalMainNodeID= mainNodeInfo.getPositionalNodeID();
			ASTNode rootNode= InferenceHelper.getRootNodeForOperation(operation);
			return ASTNodesIdentifier.getASTNodeFromPositonalID(rootNode, positionalMainNodeID);
		}
		return null;
	}

	private boolean isRelatedToVariableDeclarationFragment(ASTNode affectedNode) {
		ASTNode affectedFragment= ASTHelper.getParent(affectedNode, VariableDeclarationFragment.class);
		if (mainNode == affectedFragment) {
			return true;
		}
		if (affectedFragment == null) {
			ASTNode mainParent= mainNode.getParent();
			return mainParent == ASTHelper.getParent(affectedNode, VariableDeclarationStatement.class) ||
					mainParent == ASTHelper.getParent(affectedNode, FieldDeclaration.class);
		}
		return false;
	}

	private boolean shouldLookFromParent() {
		return this instanceof AddedVariableDeclarationRefactoringProperty ||
				this instanceof AddedFieldDeclarationRefactoringProperty ||
				this instanceof DeletedVariableDeclarationRefactoringProperty;
	}

	@Override
	public void setRefactoringID(long refactoringID) {
		for (ASTOperation relatedOperation : relatedOperations) {
			//Set the refactoring ID only if it was not already set. This ensures that the first recorded inferred refactoring
			//claims the overlapping related operations for itself.
			if (relatedOperation.getTransformationID() == -1) {
				relatedOperation.setTransformationID(refactoringID);
			}
		}
	}

	@Override
	public long getActivationTimestamp() {
		return activationTimestamp;
	}

	protected void updateActivationTimestamp(long newActivationTimestamp) {
		activationTimestamp= newActivationTimestamp;
	}

	@Override
	public void checkTimeout(long currentTimestamp) {
		if (currentTimestamp - activationTimestamp >= decayTimeThreshold) {
			disable();
		}
	}

	@Override
	public String getClassName() {
		return getClass().getSimpleName();
	}

	@Override
	public boolean isActive() {
		return isActive;
	}

	@Override
	public void disable() {
		isActive= false;
		InferredRefactoringFactory.disabledProperty(this);
		notifyRefactoringsAboutDisabledProperty(refactorings, this);
	}

	public static void notifyRefactoringsAboutDisabledProperty(Set<InferredRefactoring> refactorings, RefactoringProperty disabledProperty) {
		Iterator<InferredRefactoring> refactoringsInterator= refactorings.iterator();
		while (refactoringsInterator.hasNext()) {
			InferredRefactoring refactoring= refactoringsInterator.next();
			refactoring.disabledProperty(disabledProperty);
			if (!refactoring.isComplete()) {
				refactoringsInterator.remove();
			}
		}
	}

	@Override
	public void addRefactoring(InferredRefactoring refactoring) {
		refactorings.add(refactoring);
	}

	@Override
	public void removeRefactoring(InferredRefactoring refactoring) {
		refactorings.remove(refactoring);
	}

	@Override
	public void fireCorrected() {
		//Use a temporary collection since a corrected property might lead to a removed refactoring.
		Set<InferredRefactoring> existingRefactorings= new HashSet<InferredRefactoring>();
		existingRefactorings.addAll(refactorings);
		for (InferredRefactoring refactoring : existingRefactorings) {
			refactoring.correctedProperty(this);
		}
	}

	protected void addAttribute(String name, Object value) {
		attributes.put(name, value);
	}

	@Override
	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	@Override
	public boolean doesMatch(InferredRefactoring containingRefactoring, RefactoringProperty anotherProperty) {
		for (Entry<String, Object> entry : attributes.entrySet()) {
			String attribute= entry.getKey();
			if (!isIgnoredAttribute(attribute, containingRefactoring)) {
				Object objectToMatch= anotherProperty.getAttribute(attribute);
				if (objectToMatch != null && !objectToMatch.equals(entry.getValue())) {
					return false;
				}
				Object objectToDisjoin= anotherProperty.getAttribute(disjointAttributes.get(attribute));
				if (objectToDisjoin != null && objectToDisjoin.equals(entry.getValue())) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public boolean doesAffectSameEntity(RefactoringProperty refactoringProperty) {
		return false;
	}

	protected boolean isIgnoredAttribute(String attribute, InferredRefactoring containingRefactoring) {
		if ((containingRefactoring instanceof ExtractConstantRefactoring ||
				containingRefactoring instanceof ExtractVariableRefactoring ||
				containingRefactoring instanceof InlineVariableRefactoring ||
				containingRefactoring instanceof EncapsulateFieldRefactoring) &&
				attribute.equals(RefactoringPropertyAttributes.PARENT_ID)) {
			return true;
		}
		if ((containingRefactoring instanceof RenameClassRefactoring ||
				containingRefactoring instanceof RenameFieldRefactoring ||
				containingRefactoring instanceof RenameMethodRefactoring) &&
				attribute.equals(RefactoringPropertyAttributes.SOURCE_METHOD_ID)) {
			return true;
		}
		if (containingRefactoring instanceof ExtractMethodRefactoring &&
				attribute.equals(RefactoringPropertyAttributes.MOVE_ID)) {
			return true;
		}
		return attribute.equals(RefactoringPropertyAttributes.ENTITY_NAME_NODE_ID);
	}

	protected static boolean isVeryCloseButDistinct(AtomicRefactoringProperty property1, AtomicRefactoringProperty property2) {
		return property1.activationTimestamp != property2.activationTimestamp &&
				Math.abs(property1.activationTimestamp - property2.activationTimestamp) < closenessTimeThreshold;
	}

}
