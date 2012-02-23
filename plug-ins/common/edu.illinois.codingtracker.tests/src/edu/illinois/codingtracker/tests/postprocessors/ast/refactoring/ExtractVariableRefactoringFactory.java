/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring;

import java.util.HashSet;
import java.util.Set;

import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.operations.ast.InferredRefactoringOperation;
import edu.illinois.codingtracker.operations.ast.InferredRefactoringOperation.RefactoringKind;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties.RefactoringPropertiesFactory;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties.RefactoringProperty;



/**
 * This class handles instances of the inferred manual Extract Variable refactorings.
 * 
 * @author Stas Negara
 * 
 */
public class ExtractVariableRefactoringFactory {


	//TODO: Should be shared among all refactoring kinds.
	private static long refactoringID= 1;

	private static RefactoringKind refactoringKind= RefactoringKind.EXTRACT_LOCAL_VARIABLE;

	private static final Set<ExtractVariableRefactoring> currentRefactorings= new HashSet<ExtractVariableRefactoring>();


	/**
	 * Should be called before processing a sequence.
	 */
	public static void resetCurrentRefactorings() {
		refactoringID= 1;
		currentRefactorings.clear();
	}

	public static InferredRefactoringOperation handleASTOperation(ASTOperation operation) {
		//TODO: Should remove refactorings after some time threshold (i.e., after a reasonable time for a manual refactoring).
		RefactoringProperty refactoringProperty= RefactoringPropertiesFactory.createProperty(operation);
		if (refactoringProperty != null) {
			ExtractVariableRefactoring completeRefactoring= addProperty(refactoringProperty);
			if (completeRefactoring != null) {
				InferredRefactoringOperation inferredRefactoring= new InferredRefactoringOperation(refactoringKind, refactoringID++, completeRefactoring.getArguments(), operation.getTime());
				disableProperties(completeRefactoring);
				return inferredRefactoring;
			}
			//TODO: Revise to avoid creation of unnecessary object.
			ExtractVariableRefactoring newRefactoring= new ExtractVariableRefactoring();
			if (newRefactoring.canBePart(refactoringProperty)) {
				currentRefactorings.add(newRefactoring.addProperty(refactoringProperty));
			}
		}
		return null;
	}

	private static ExtractVariableRefactoring addProperty(RefactoringProperty refactoringProperty) {
		Set<ExtractVariableRefactoring> newRefactorings= new HashSet<ExtractVariableRefactoring>();
		for (ExtractVariableRefactoring refactoring : currentRefactorings) {
			if (refactoring.canBePart(refactoringProperty)) {
				ExtractVariableRefactoring newRefactoring= refactoring.addProperty(refactoringProperty);
				newRefactorings.add(newRefactoring);
				if (newRefactoring.isComplete()) {
					return newRefactoring;
				}
			}
		}
		currentRefactorings.addAll(newRefactorings);
		return null;
	}

	private static void disableProperties(ExtractVariableRefactoring completeRefactoring) {
		//TODO: After disabling some properties, there could remain duplicated refactorings in currentRefactorings, i.e.,
		//refactorings with exactly the same set of properties, which could lead to a huge performance overhead.
		completeRefactoring.disableProperties();
		Set<ExtractVariableRefactoring> disabledRefactorings= new HashSet<ExtractVariableRefactoring>();
		for (ExtractVariableRefactoring refactoring : currentRefactorings) {
			if (refactoring.checkDisabled()) {
				disabledRefactorings.add(refactoring);
			}
		}
		currentRefactorings.removeAll(disabledRefactorings);
	}

}
