/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring;

import java.util.HashSet;
import java.util.Set;

import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.operations.ast.InferredRefactoringOperation;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties.RefactoringPropertiesFactory;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties.RefactoringProperty;



/**
 * This class handles instances of the inferred refactorings.
 * 
 * @author Stas Negara
 * 
 */
public class InferredRefactoringFactory {

	private static long refactoringID= 1;

	private static final Set<InferredRefactoring> currentRefactorings= new HashSet<InferredRefactoring>();


	/**
	 * Should be called before processing a sequence.
	 */
	public static void resetCurrentRefactorings() {
		refactoringID= 1;
		currentRefactorings.clear();
	}

	public static InferredRefactoringOperation handleASTOperation(ASTOperation operation) {
		//TODO: Should remove refactorings after some time threshold (i.e., after a reasonable time for a manual refactoring).
		Set<RefactoringProperty> refactoringProperties= RefactoringPropertiesFactory.retrieveProperties(operation);
		for (RefactoringProperty refactoringProperty : refactoringProperties) {
			InferredRefactoring inferredRefactoring= addProperty(refactoringProperty);
			if (inferredRefactoring != null) {
				InferredRefactoringOperation refactoringOperation=
							new InferredRefactoringOperation(inferredRefactoring.getKind(), refactoringID++, inferredRefactoring.getArguments(), operation.getTime());
				disableProperties(inferredRefactoring);
				return refactoringOperation;
			}
			if (ExtractVariableRefactoring.isAcceptableProperty(refactoringProperty)) {
				currentRefactorings.add(ExtractVariableRefactoring.createRefactoring(refactoringProperty));
			}
			if (InlineVariableRefactoring.isAcceptableProperty(refactoringProperty)) {
				currentRefactorings.add(InlineVariableRefactoring.createRefactoring(refactoringProperty));
			}
			if (RenameVariableRefactoring.isAcceptableProperty(refactoringProperty)) {
				currentRefactorings.add(RenameVariableRefactoring.createRefactoring(refactoringProperty));
			}
			if (RenameFieldRefactoring.isAcceptableProperty(refactoringProperty)) {
				currentRefactorings.add(RenameFieldRefactoring.createRefactoring(refactoringProperty));
			}
			if (RenameMethodRefactoring.isAcceptableProperty(refactoringProperty)) {
				currentRefactorings.add(RenameMethodRefactoring.createRefactoring(refactoringProperty));
			}
			if (RenameClassRefactoring.isAcceptableProperty(refactoringProperty)) {
				currentRefactorings.add(RenameClassRefactoring.createRefactoring(refactoringProperty));
			}
			if (PromoteTempRefactoring.isAcceptableProperty(refactoringProperty)) {
				currentRefactorings.add(PromoteTempRefactoring.createRefactoring(refactoringProperty));
			}
			if (ExtractConstantRefactoring.isAcceptableProperty(refactoringProperty)) {
				currentRefactorings.add(ExtractConstantRefactoring.createRefactoring(refactoringProperty));
			}
			if (ExtractMethodRefactoring.isAcceptableProperty(refactoringProperty)) {
				currentRefactorings.add(ExtractMethodRefactoring.createRefactoring(refactoringProperty));
			}
			if (EncapsulateFieldRefactoring.isAcceptableProperty(refactoringProperty)) {
				currentRefactorings.add(EncapsulateFieldRefactoring.createRefactoring(refactoringProperty));
			}
		}
		return null;
	}

	private static InferredRefactoring addProperty(RefactoringProperty refactoringProperty) {
		Set<InferredRefactoring> newRefactorings= new HashSet<InferredRefactoring>();
		for (InferredRefactoring refactoring : currentRefactorings) {
			if (refactoring.canBePart(refactoringProperty)) {
				InferredRefactoring newRefactoring= refactoring.addProperty(refactoringProperty);
				if (newRefactoring != null) {
					newRefactorings.add(newRefactoring);
					if (newRefactoring.isComplete()) {
						return newRefactoring;
					}
				}
			}
		}
		currentRefactorings.addAll(newRefactorings);
		return null;
	}

	private static void disableProperties(InferredRefactoring refactoring) {
		//TODO: After disabling some properties, there could remain duplicated refactorings in currentRefactorings, i.e.,
		//refactorings with exactly the same set of properties, which could lead to a huge performance overhead.
		refactoring.disableProperties();
		Set<InferredRefactoring> disabledRefactorings= new HashSet<InferredRefactoring>();
		for (InferredRefactoring currentRefactoring : currentRefactorings) {
			if (currentRefactoring.checkDisabled()) {
				disabledRefactorings.add(currentRefactoring);
			}
		}
		currentRefactorings.removeAll(disabledRefactorings);
	}

}
