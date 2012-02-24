/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring;

import java.util.HashMap;
import java.util.Map;

import edu.illinois.codingtracker.operations.ast.InferredRefactoringOperation.RefactoringKind;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties.ChangedVariableNameInDeclarationRefactoringProperty;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties.ChangedVariableNameInUsageRefactoringProperty;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties.RefactoringProperty;



/**
 * This class represents an instance of partially or fully inferred manual Rename Local Variable
 * refactoring.
 * 
 * @author Stas Negara
 * 
 */
public class RenameVariableRefactoring extends InferredRefactoring {

	private String oldVariableName;

	private String newVariableName;

	private ChangedVariableNameInDeclarationRefactoringProperty changedInDeclaration;

	private ChangedVariableNameInUsageRefactoringProperty changedInUsage;


	private RenameVariableRefactoring() {

	}

	public static RenameVariableRefactoring createRefactoring(RefactoringProperty refactoringProperty) {
		if (!isAcceptableProperty(refactoringProperty)) {
			throw new RuntimeException("Can not create RenameVariableRefactoring for property: " + refactoringProperty);
		}
		RenameVariableRefactoring newRefactoring= new RenameVariableRefactoring();
		addProperty(newRefactoring, refactoringProperty);
		return newRefactoring;
	}

	public static boolean isAcceptableProperty(RefactoringProperty refactoringProperty) {
		return refactoringProperty instanceof ChangedVariableNameInDeclarationRefactoringProperty ||
				refactoringProperty instanceof ChangedVariableNameInUsageRefactoringProperty;
	}

	@Override
	public RefactoringKind getKind() {
		return RefactoringKind.RENAME_LOCAL_VARIABLE;
	}

	@Override
	public boolean isComplete() {
		return changedInDeclaration != null && changedInUsage != null;
	}

	@Override
	protected boolean isDisabled() {
		return changedInDeclaration == null && changedInUsage == null;
	}

	@Override
	public boolean canBePart(RefactoringProperty refactoringProperty) {
		if (refactoringProperty instanceof ChangedVariableNameInDeclarationRefactoringProperty) {
			if (changedInDeclaration != null) {
				return false;
			}
			ChangedVariableNameInDeclarationRefactoringProperty changedInDeclaration= (ChangedVariableNameInDeclarationRefactoringProperty)refactoringProperty;
			if (oldVariableName != null && !oldVariableName.equals(changedInDeclaration.getOldVariableName())) {
				return false;
			}
			if (newVariableName != null && !newVariableName.equals(changedInDeclaration.getNewVariableName())) {
				return false;
			}
			return true;
		} else if (refactoringProperty instanceof ChangedVariableNameInUsageRefactoringProperty) {
			if (changedInUsage != null) {
				return false;
			}
			ChangedVariableNameInUsageRefactoringProperty changedInUsage= (ChangedVariableNameInUsageRefactoringProperty)refactoringProperty;
			if (oldVariableName != null && !oldVariableName.equals(changedInUsage.getOldVariableName())) {
				return false;
			}
			if (newVariableName != null && !newVariableName.equals(changedInUsage.getNewVariableName())) {
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
		RenameVariableRefactoring resultRefactoring= createCopy();
		addProperty(resultRefactoring, refactoringProperty);
		return resultRefactoring;
	}

	private static void addProperty(RenameVariableRefactoring refactoring, RefactoringProperty refactoringProperty) {
		if (refactoringProperty instanceof ChangedVariableNameInDeclarationRefactoringProperty) {
			ChangedVariableNameInDeclarationRefactoringProperty changedInDeclaration= (ChangedVariableNameInDeclarationRefactoringProperty)refactoringProperty;
			refactoring.changedInDeclaration= changedInDeclaration;
			refactoring.oldVariableName= changedInDeclaration.getOldVariableName();
			refactoring.newVariableName= changedInDeclaration.getNewVariableName();
		} else if (refactoringProperty instanceof ChangedVariableNameInUsageRefactoringProperty) {
			ChangedVariableNameInUsageRefactoringProperty changedInUsage= (ChangedVariableNameInUsageRefactoringProperty)refactoringProperty;
			refactoring.changedInUsage= changedInUsage;
			refactoring.oldVariableName= changedInUsage.getOldVariableName();
			refactoring.newVariableName= changedInUsage.getNewVariableName();
		}
	}

	private RenameVariableRefactoring createCopy() {
		RenameVariableRefactoring copyRefactoring= new RenameVariableRefactoring();
		copyRefactoring.oldVariableName= oldVariableName;
		copyRefactoring.newVariableName= newVariableName;
		copyRefactoring.changedInDeclaration= changedInDeclaration;
		copyRefactoring.changedInUsage= changedInUsage;
		return copyRefactoring;
	}

	@Override
	public void disableProperties() {
		changedInDeclaration.disable();
		changedInUsage.disable();
	}

	@Override
	public boolean checkDisabled() {
		if (changedInDeclaration != null && !changedInDeclaration.isActive()) {
			changedInDeclaration= null;
		}
		if (changedInUsage != null && !changedInUsage.isActive()) {
			changedInUsage= null;
		}
		resetState();
		return isDisabled();
	}

	private void resetState() {
		oldVariableName= null;
		newVariableName= null;
		if (changedInDeclaration != null) {
			oldVariableName= changedInDeclaration.getOldVariableName();
			newVariableName= changedInDeclaration.getNewVariableName();
		}
		if (changedInUsage != null) {
			oldVariableName= changedInUsage.getOldVariableName();
			newVariableName= changedInUsage.getNewVariableName();
		}
	}

	@Override
	public Map<String, String> getArguments() {
		Map<String, String> arguments= new HashMap<String, String>();
		arguments.put("OldVariableName", oldVariableName);
		arguments.put("NewVariableName", newVariableName);
		return arguments;
	}

}
