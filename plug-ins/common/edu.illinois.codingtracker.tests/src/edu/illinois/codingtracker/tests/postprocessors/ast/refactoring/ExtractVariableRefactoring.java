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

	public void addProperty(RefactoringProperty refactoringProperty) {
		if (!canBePart(refactoringProperty)) {
			throw new RuntimeException("Can not add property: " + refactoringProperty);
		}
		//It's OK to re-assign the values (e.g., variable name, move ID, etc.) since the check at the beginning 
		//of the method ensures that the fields are either not initialized or match anyway.
		if (refactoringProperty instanceof MovedToInitializationRefactoringProperty) {
			movedToInitialization= (MovedToInitializationRefactoringProperty)refactoringProperty;
			movedNode= movedToInitialization.getMovedNode();
			moveID= movedToInitialization.getMoveID();
			variableName= movedToInitialization.getVariableName();
		} else if (refactoringProperty instanceof DeclaredVariableRefactoringProperty) {
			declaredVariable= (DeclaredVariableRefactoringProperty)refactoringProperty;
			variableName= declaredVariable.getVariableName();
		} else if (refactoringProperty instanceof MovedFromUsageRefactoringProperty) {
			movedFromUsage= (MovedFromUsageRefactoringProperty)refactoringProperty;
			movedNode= movedFromUsage.getMovedNode();
			moveID= movedFromUsage.getMoveID();
			parentID= movedFromUsage.getParentID();
		} else if (refactoringProperty instanceof AddedVariableReferenceRefactoringProperty) {
			addedVariableReference= (AddedVariableReferenceRefactoringProperty)refactoringProperty;
			variableName= addedVariableReference.getVariableName();
			parentID= addedVariableReference.getParentID();
		}
	}

	public boolean isComplete() {
		return movedToInitialization != null && declaredVariable != null && movedFromUsage != null && addedVariableReference != null;
	}

	public Map<String, String> getArguments() {
		Map<String, String> arguments= new HashMap<String, String>();
		arguments.put("VariableName", variableName);
		arguments.put("ExtractedValue", movedNode.getNodeText());
		return arguments;
	}

}
