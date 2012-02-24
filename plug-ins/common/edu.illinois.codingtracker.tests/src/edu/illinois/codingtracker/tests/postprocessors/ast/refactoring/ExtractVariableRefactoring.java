/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring;

import java.util.HashMap;
import java.util.Map;

import edu.illinois.codingtracker.operations.ast.InferredRefactoringOperation.RefactoringKind;
import edu.illinois.codingtracker.tests.postprocessors.ast.move.NodeDescriptor;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties.AddedVariableReferenceRefactoringProperty;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties.AddedVariableDeclarationRefactoringProperty;
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
public class ExtractVariableRefactoring implements InferredRefactoring {

	private NodeDescriptor movedNode;

	private long moveID= -1;

	private String variableName;

	private long parentID= -1;

	private MovedToInitializationRefactoringProperty movedToInitialization;

	private AddedVariableDeclarationRefactoringProperty addedVariableDeclaration;

	private MovedFromUsageRefactoringProperty movedFromUsage;

	private AddedVariableReferenceRefactoringProperty addedVariableReference;


	private ExtractVariableRefactoring() {

	}

	public static ExtractVariableRefactoring createRefactoring(RefactoringProperty refactoringProperty) {
		if (!isAcceptableProperty(refactoringProperty)) {
			throw new RuntimeException("Can not create ExtractVariableRefactoring for property: " + refactoringProperty);
		}
		ExtractVariableRefactoring newRefactoring= new ExtractVariableRefactoring();
		addProperty(newRefactoring, refactoringProperty);
		return newRefactoring;
	}

	public static boolean isAcceptableProperty(RefactoringProperty refactoringProperty) {
		return refactoringProperty instanceof MovedToInitializationRefactoringProperty ||
				refactoringProperty instanceof AddedVariableDeclarationRefactoringProperty ||
				refactoringProperty instanceof MovedFromUsageRefactoringProperty ||
				refactoringProperty instanceof AddedVariableReferenceRefactoringProperty;
	}

	@Override
	public RefactoringKind getKind() {
		return RefactoringKind.EXTRACT_LOCAL_VARIABLE;
	}

	@Override
	public boolean isComplete() {
		return movedToInitialization != null && addedVariableDeclaration != null && movedFromUsage != null && addedVariableReference != null;
	}

	@Override
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
		} else if (refactoringProperty instanceof AddedVariableDeclarationRefactoringProperty) {
			if (addedVariableDeclaration != null) {
				return false;
			}
			AddedVariableDeclarationRefactoringProperty addedVariableDeclaration= (AddedVariableDeclarationRefactoringProperty)refactoringProperty;
			if (variableName != null && !variableName.equals(addedVariableDeclaration.getVariableName())) {
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

	@Override
	public InferredRefactoring addProperty(RefactoringProperty refactoringProperty) {
		if (!canBePart(refactoringProperty)) {
			throw new RuntimeException("Can not add property: " + refactoringProperty);
		}
		ExtractVariableRefactoring resultRefactoring= createCopy();
		addProperty(resultRefactoring, refactoringProperty);
		return resultRefactoring;
	}

	private static void addProperty(ExtractVariableRefactoring refactoring, RefactoringProperty refactoringProperty) {
		if (refactoringProperty instanceof MovedToInitializationRefactoringProperty) {
			MovedToInitializationRefactoringProperty movedToInitialization= (MovedToInitializationRefactoringProperty)refactoringProperty;
			refactoring.movedToInitialization= movedToInitialization;
			refactoring.movedNode= movedToInitialization.getMovedNode();
			refactoring.moveID= movedToInitialization.getMoveID();
			refactoring.variableName= movedToInitialization.getVariableName();
		} else if (refactoringProperty instanceof AddedVariableDeclarationRefactoringProperty) {
			AddedVariableDeclarationRefactoringProperty addedVariableDeclaration= (AddedVariableDeclarationRefactoringProperty)refactoringProperty;
			refactoring.addedVariableDeclaration= addedVariableDeclaration;
			refactoring.variableName= addedVariableDeclaration.getVariableName();
		} else if (refactoringProperty instanceof MovedFromUsageRefactoringProperty) {
			MovedFromUsageRefactoringProperty movedFromUsage= (MovedFromUsageRefactoringProperty)refactoringProperty;
			refactoring.movedFromUsage= movedFromUsage;
			refactoring.movedNode= movedFromUsage.getMovedNode();
			refactoring.moveID= movedFromUsage.getMoveID();
			refactoring.parentID= movedFromUsage.getParentID();
		} else if (refactoringProperty instanceof AddedVariableReferenceRefactoringProperty) {
			AddedVariableReferenceRefactoringProperty addedVariableReference= (AddedVariableReferenceRefactoringProperty)refactoringProperty;
			refactoring.addedVariableReference= addedVariableReference;
			refactoring.variableName= addedVariableReference.getVariableName();
			refactoring.parentID= addedVariableReference.getParentID();
		}
	}

	private ExtractVariableRefactoring createCopy() {
		ExtractVariableRefactoring copyRefactoring= new ExtractVariableRefactoring();
		copyRefactoring.movedNode= movedNode;
		copyRefactoring.moveID= moveID;
		copyRefactoring.variableName= variableName;
		copyRefactoring.parentID= parentID;
		copyRefactoring.movedToInitialization= movedToInitialization;
		copyRefactoring.addedVariableDeclaration= addedVariableDeclaration;
		copyRefactoring.movedFromUsage= movedFromUsage;
		copyRefactoring.addedVariableReference= addedVariableReference;
		return copyRefactoring;
	}

	@Override
	public void disableProperties() {
		movedToInitialization.disable();
		addedVariableDeclaration.disable();
		movedFromUsage.disable();
		addedVariableReference.disable();
	}

	@Override
	public boolean checkDisabled() {
		if (movedToInitialization != null && !movedToInitialization.isActive()) {
			movedToInitialization= null;
		}
		if (addedVariableDeclaration != null && !addedVariableDeclaration.isActive()) {
			addedVariableDeclaration= null;
		}
		if (movedFromUsage != null && !movedFromUsage.isActive()) {
			movedFromUsage= null;
		}
		if (addedVariableReference != null && !addedVariableReference.isActive()) {
			addedVariableReference= null;
		}
		resetState();
		return movedToInitialization == null && addedVariableDeclaration == null && movedFromUsage == null && addedVariableReference == null;
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
		if (addedVariableDeclaration != null) {
			variableName= addedVariableDeclaration.getVariableName();
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

	@Override
	public Map<String, String> getArguments() {
		Map<String, String> arguments= new HashMap<String, String>();
		arguments.put("VariableName", variableName);
		arguments.put("ExtractedValue", movedNode.getNodeText());
		return arguments;
	}

}
