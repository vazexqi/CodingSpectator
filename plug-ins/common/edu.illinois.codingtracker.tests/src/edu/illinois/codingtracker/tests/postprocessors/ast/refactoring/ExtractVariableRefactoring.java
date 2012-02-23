/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring;

import java.util.HashMap;
import java.util.Map;

import edu.illinois.codingtracker.tests.postprocessors.ast.move.NodeDescriptor;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties.AddedVariableReferenceRefactoringProperty;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties.DeclaredVariableRefactoringProperty;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties.MovedFromUsageRefactoringProperty;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties.MovedToInitializationRefactoringProperty;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties.RefactoringProperty;



/**
 * This class represents an instance of partially or fully inferred manual Extract Variable
 * refactoring.
 * 
 * @author Stas Negara
 * 
 */
public class ExtractVariableRefactoring {

	private NodeDescriptor movedNode;

	private long moveID= -1;

	private String variableName;

	private long parentID= -1;

	private MovedToInitializationRefactoringProperty movedToInitialization;

	private DeclaredVariableRefactoringProperty declaredVariable;

	private MovedFromUsageRefactoringProperty movedFromUsage;

	private AddedVariableReferenceRefactoringProperty addedVariableReference;


	public boolean canBePart(RefactoringProperty refactoringProperty) {
		if (refactoringProperty instanceof MovedToInitializationRefactoringProperty) {
			if (movedToInitialization != null) {
				return false;
			}
			MovedToInitializationRefactoringProperty movedToInitialization= (MovedToInitializationRefactoringProperty)refactoringProperty;
			if (movedNode != null && !movedNode.equals(movedToInitialization.getMovedNode())) {
				return false;
			}
			if (moveID != -1 && moveID != movedToInitialization.getMoveID()) {
				return false;
			}
			if (variableName != null && !variableName.equals(movedToInitialization.getVariableName())) {
				return false;
			}
			return true;
		} else if (refactoringProperty instanceof DeclaredVariableRefactoringProperty) {
			if (declaredVariable != null) {
				return false;
			}
			DeclaredVariableRefactoringProperty declaredVariable= (DeclaredVariableRefactoringProperty)refactoringProperty;
			if (variableName != null && !variableName.equals(declaredVariable.getVariableName())) {
				return false;
			}
			return true;
		} else if (refactoringProperty instanceof MovedFromUsageRefactoringProperty) {
			if (movedFromUsage != null) {
				return false;
			}
			MovedFromUsageRefactoringProperty movedFromUsage= (MovedFromUsageRefactoringProperty)refactoringProperty;
			if (movedNode != null && !movedNode.equals(movedFromUsage.getMovedNode())) {
				return false;
			}
			if (moveID != -1 && moveID != movedFromUsage.getMoveID()) {
				return false;
			}
			if (parentID != -1 && parentID != movedFromUsage.getParentID()) {
				return false;
			}
			return true;
		} else if (refactoringProperty instanceof AddedVariableReferenceRefactoringProperty) {
			if (addedVariableReference != null) {
				return false;
			}
			AddedVariableReferenceRefactoringProperty addedVariableReference= (AddedVariableReferenceRefactoringProperty)refactoringProperty;
			if (variableName != null && !variableName.equals(addedVariableReference.getVariableName())) {
				return false;
			}
			if (parentID != -1 && parentID != addedVariableReference.getParentID()) {
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * Does not change this refactoring, but rather returns a new one, with this refactoring
	 * property added to it.
	 * 
	 * @param refactoringProperty
	 */
	public ExtractVariableRefactoring addProperty(RefactoringProperty refactoringProperty) {
		if (!canBePart(refactoringProperty)) {
			throw new RuntimeException("Can not add property: " + refactoringProperty);
		}
		ExtractVariableRefactoring resultRefactoring= createCopy();
		if (refactoringProperty instanceof MovedToInitializationRefactoringProperty) {
			MovedToInitializationRefactoringProperty movedToInitialization= (MovedToInitializationRefactoringProperty)refactoringProperty;
			resultRefactoring.movedToInitialization= movedToInitialization;
			resultRefactoring.movedNode= movedToInitialization.getMovedNode();
			resultRefactoring.moveID= movedToInitialization.getMoveID();
			resultRefactoring.variableName= movedToInitialization.getVariableName();
		} else if (refactoringProperty instanceof DeclaredVariableRefactoringProperty) {
			DeclaredVariableRefactoringProperty declaredVariable= (DeclaredVariableRefactoringProperty)refactoringProperty;
			resultRefactoring.declaredVariable= declaredVariable;
			resultRefactoring.variableName= declaredVariable.getVariableName();
		} else if (refactoringProperty instanceof MovedFromUsageRefactoringProperty) {
			MovedFromUsageRefactoringProperty movedFromUsage= (MovedFromUsageRefactoringProperty)refactoringProperty;
			resultRefactoring.movedFromUsage= movedFromUsage;
			resultRefactoring.movedNode= movedFromUsage.getMovedNode();
			resultRefactoring.moveID= movedFromUsage.getMoveID();
			resultRefactoring.parentID= movedFromUsage.getParentID();
		} else if (refactoringProperty instanceof AddedVariableReferenceRefactoringProperty) {
			AddedVariableReferenceRefactoringProperty addedVariableReference= (AddedVariableReferenceRefactoringProperty)refactoringProperty;
			resultRefactoring.addedVariableReference= addedVariableReference;
			resultRefactoring.variableName= addedVariableReference.getVariableName();
			resultRefactoring.parentID= addedVariableReference.getParentID();
		}
		return resultRefactoring;
	}

	private ExtractVariableRefactoring createCopy() {
		ExtractVariableRefactoring copyRefactoring= new ExtractVariableRefactoring();
		copyRefactoring.movedNode= movedNode;
		copyRefactoring.moveID= moveID;
		copyRefactoring.variableName= variableName;
		copyRefactoring.parentID= parentID;
		copyRefactoring.movedToInitialization= movedToInitialization;
		copyRefactoring.declaredVariable= declaredVariable;
		copyRefactoring.movedFromUsage= movedFromUsage;
		copyRefactoring.addedVariableReference= addedVariableReference;
		return copyRefactoring;
	}

	public boolean isComplete() {
		return movedToInitialization != null && declaredVariable != null && movedFromUsage != null && addedVariableReference != null;
	}

	public void disableProperties() {
		movedToInitialization.disable();
		declaredVariable.disable();
		movedFromUsage.disable();
		addedVariableReference.disable();
	}

	public boolean checkDisabled() {
		if (movedToInitialization != null && !movedToInitialization.isActive()) {
			movedToInitialization= null;
		}
		if (declaredVariable != null && !declaredVariable.isActive()) {
			declaredVariable= null;
		}
		if (movedFromUsage != null && !movedFromUsage.isActive()) {
			movedFromUsage= null;
		}
		if (addedVariableReference != null && !addedVariableReference.isActive()) {
			addedVariableReference= null;
		}
		resetState();
		return movedToInitialization == null && declaredVariable == null && movedFromUsage == null && addedVariableReference == null;
	}

	private void resetState() {
		movedNode= null;
		moveID= -1;
		variableName= null;
		parentID= -1;
		if (movedToInitialization != null) {
			movedNode= movedToInitialization.getMovedNode();
			moveID= movedToInitialization.getMoveID();
			variableName= movedToInitialization.getVariableName();
		}
		if (declaredVariable != null) {
			variableName= declaredVariable.getVariableName();
		}
		if (movedFromUsage != null) {
			movedNode= movedFromUsage.getMovedNode();
			moveID= movedFromUsage.getMoveID();
			parentID= movedFromUsage.getParentID();
		}
		if (addedVariableReference != null) {
			variableName= addedVariableReference.getVariableName();
			parentID= addedVariableReference.getParentID();
		}
	}

	public Map<String, String> getArguments() {
		Map<String, String> arguments= new HashMap<String, String>();
		arguments.put("VariableName", variableName);
		arguments.put("ExtractedValue", movedNode.getNodeText());
		return arguments;
	}

}
