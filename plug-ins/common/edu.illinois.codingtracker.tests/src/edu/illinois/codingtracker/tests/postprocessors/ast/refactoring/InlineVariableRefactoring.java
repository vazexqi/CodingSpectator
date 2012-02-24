/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring;

import java.util.HashMap;
import java.util.Map;

import edu.illinois.codingtracker.operations.ast.InferredRefactoringOperation.RefactoringKind;
import edu.illinois.codingtracker.tests.postprocessors.ast.move.NodeDescriptor;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties.DeletedVariableDeclarationRefactoringProperty;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties.DeletedVariableReferenceRefactoringProperty;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties.MovedFromInitializationRefactoringProperty;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties.MovedToUsageRefactoringProperty;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties.RefactoringProperty;



/**
 * This class represents an instance of partially or fully inferred manual Inline Variable
 * refactoring.
 * 
 * @author Stas Negara
 * 
 */
public class InlineVariableRefactoring implements InferredRefactoring {

	private NodeDescriptor movedNode;

	private long moveID= -1;

	private String variableName;

	private long parentID= -1;

	private MovedFromInitializationRefactoringProperty movedFromInitialization;

	private DeletedVariableDeclarationRefactoringProperty deletedVariableDeclaration;

	private MovedToUsageRefactoringProperty movedToUsage;

	private DeletedVariableReferenceRefactoringProperty deletedVariableReference;


	private InlineVariableRefactoring() {

	}

	public static InlineVariableRefactoring createRefactoring(RefactoringProperty refactoringProperty) {
		if (!isAcceptableProperty(refactoringProperty)) {
			throw new RuntimeException("Can not create InlineVariableRefactoring for property: " + refactoringProperty);
		}
		InlineVariableRefactoring newRefactoring= new InlineVariableRefactoring();
		addProperty(newRefactoring, refactoringProperty);
		return newRefactoring;
	}

	public static boolean isAcceptableProperty(RefactoringProperty refactoringProperty) {
		return refactoringProperty instanceof MovedFromInitializationRefactoringProperty ||
				refactoringProperty instanceof DeletedVariableDeclarationRefactoringProperty ||
				refactoringProperty instanceof MovedToUsageRefactoringProperty ||
				refactoringProperty instanceof DeletedVariableReferenceRefactoringProperty;
	}

	@Override
	public RefactoringKind getKind() {
		return RefactoringKind.INLINE_LOCAL_VARIABLE;
	}

	@Override
	public boolean isComplete() {
		return movedFromInitialization != null && deletedVariableDeclaration != null && movedToUsage != null && deletedVariableReference != null;
	}

	@Override
	public boolean canBePart(RefactoringProperty refactoringProperty) {
		if (refactoringProperty instanceof MovedFromInitializationRefactoringProperty) {
			if (movedFromInitialization != null) {
				return false;
			}
			MovedFromInitializationRefactoringProperty movedFromInitialization= (MovedFromInitializationRefactoringProperty)refactoringProperty;
			if (movedNode != null && !movedNode.equals(movedFromInitialization.getMovedNode())) {
				return false;
			}
			if (moveID != -1 && moveID != movedFromInitialization.getMoveID()) {
				return false;
			}
			if (variableName != null && !variableName.equals(movedFromInitialization.getVariableName())) {
				return false;
			}
			return true;
		} else if (refactoringProperty instanceof DeletedVariableDeclarationRefactoringProperty) {
			if (deletedVariableDeclaration != null) {
				return false;
			}
			DeletedVariableDeclarationRefactoringProperty deletedVariableDeclaration= (DeletedVariableDeclarationRefactoringProperty)refactoringProperty;
			if (variableName != null && !variableName.equals(deletedVariableDeclaration.getVariableName())) {
				return false;
			}
			return true;
		} else if (refactoringProperty instanceof MovedToUsageRefactoringProperty) {
			if (movedToUsage != null) {
				return false;
			}
			MovedToUsageRefactoringProperty movedToUsage= (MovedToUsageRefactoringProperty)refactoringProperty;
			if (movedNode != null && !movedNode.equals(movedToUsage.getMovedNode())) {
				return false;
			}
			if (moveID != -1 && moveID != movedToUsage.getMoveID()) {
				return false;
			}
			if (parentID != -1 && parentID != movedToUsage.getParentID()) {
				return false;
			}
			return true;
		} else if (refactoringProperty instanceof DeletedVariableReferenceRefactoringProperty) {
			if (deletedVariableReference != null) {
				return false;
			}
			DeletedVariableReferenceRefactoringProperty deletedVariableReference= (DeletedVariableReferenceRefactoringProperty)refactoringProperty;
			if (variableName != null && !variableName.equals(deletedVariableReference.getVariableName())) {
				return false;
			}
			if (parentID != -1 && parentID != deletedVariableReference.getParentID()) {
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
		InlineVariableRefactoring resultRefactoring= createCopy();
		addProperty(resultRefactoring, refactoringProperty);
		return resultRefactoring;
	}

	private static void addProperty(InlineVariableRefactoring refactoring, RefactoringProperty refactoringProperty) {
		if (refactoringProperty instanceof MovedFromInitializationRefactoringProperty) {
			MovedFromInitializationRefactoringProperty movedFromInitialization= (MovedFromInitializationRefactoringProperty)refactoringProperty;
			refactoring.movedFromInitialization= movedFromInitialization;
			refactoring.movedNode= movedFromInitialization.getMovedNode();
			refactoring.moveID= movedFromInitialization.getMoveID();
			refactoring.variableName= movedFromInitialization.getVariableName();
		} else if (refactoringProperty instanceof DeletedVariableDeclarationRefactoringProperty) {
			DeletedVariableDeclarationRefactoringProperty deletedVariableDeclaration= (DeletedVariableDeclarationRefactoringProperty)refactoringProperty;
			refactoring.deletedVariableDeclaration= deletedVariableDeclaration;
			refactoring.variableName= deletedVariableDeclaration.getVariableName();
		} else if (refactoringProperty instanceof MovedToUsageRefactoringProperty) {
			MovedToUsageRefactoringProperty movedToUsage= (MovedToUsageRefactoringProperty)refactoringProperty;
			refactoring.movedToUsage= movedToUsage;
			refactoring.movedNode= movedToUsage.getMovedNode();
			refactoring.moveID= movedToUsage.getMoveID();
			refactoring.parentID= movedToUsage.getParentID();
		} else if (refactoringProperty instanceof DeletedVariableReferenceRefactoringProperty) {
			DeletedVariableReferenceRefactoringProperty deletedVariableReference= (DeletedVariableReferenceRefactoringProperty)refactoringProperty;
			refactoring.deletedVariableReference= deletedVariableReference;
			refactoring.variableName= deletedVariableReference.getVariableName();
			refactoring.parentID= deletedVariableReference.getParentID();
		}
	}

	private InlineVariableRefactoring createCopy() {
		InlineVariableRefactoring copyRefactoring= new InlineVariableRefactoring();
		copyRefactoring.movedNode= movedNode;
		copyRefactoring.moveID= moveID;
		copyRefactoring.variableName= variableName;
		copyRefactoring.parentID= parentID;
		copyRefactoring.movedFromInitialization= movedFromInitialization;
		copyRefactoring.deletedVariableDeclaration= deletedVariableDeclaration;
		copyRefactoring.movedToUsage= movedToUsage;
		copyRefactoring.deletedVariableReference= deletedVariableReference;
		return copyRefactoring;
	}

	@Override
	public void disableProperties() {
		movedFromInitialization.disable();
		deletedVariableDeclaration.disable();
		movedToUsage.disable();
		deletedVariableReference.disable();
	}

	@Override
	public boolean checkDisabled() {
		if (movedFromInitialization != null && !movedFromInitialization.isActive()) {
			movedFromInitialization= null;
		}
		if (deletedVariableDeclaration != null && !deletedVariableDeclaration.isActive()) {
			deletedVariableDeclaration= null;
		}
		if (movedToUsage != null && !movedToUsage.isActive()) {
			movedToUsage= null;
		}
		if (deletedVariableReference != null && !deletedVariableReference.isActive()) {
			deletedVariableReference= null;
		}
		resetState();
		return movedFromInitialization == null && deletedVariableDeclaration == null && movedToUsage == null && deletedVariableReference == null;
	}

	private void resetState() {
		movedNode= null;
		moveID= -1;
		variableName= null;
		parentID= -1;
		if (movedFromInitialization != null) {
			movedNode= movedFromInitialization.getMovedNode();
			moveID= movedFromInitialization.getMoveID();
			variableName= movedFromInitialization.getVariableName();
		}
		if (deletedVariableDeclaration != null) {
			variableName= deletedVariableDeclaration.getVariableName();
		}
		if (movedToUsage != null) {
			movedNode= movedToUsage.getMovedNode();
			moveID= movedToUsage.getMoveID();
			parentID= movedToUsage.getParentID();
		}
		if (deletedVariableReference != null) {
			variableName= deletedVariableReference.getVariableName();
			parentID= deletedVariableReference.getParentID();
		}
	}

	@Override
	public Map<String, String> getArguments() {
		Map<String, String> arguments= new HashMap<String, String>();
		arguments.put("VariableName", variableName);
		arguments.put("InlinedValue", movedNode.getNodeText());
		return arguments;
	}

}
