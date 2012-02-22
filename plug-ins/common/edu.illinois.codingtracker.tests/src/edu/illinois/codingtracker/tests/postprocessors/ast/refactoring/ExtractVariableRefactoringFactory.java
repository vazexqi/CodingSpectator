/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring;

import java.util.HashSet;
import java.util.Set;

import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.operations.ast.ManualRefactoringOperation;
import edu.illinois.codingtracker.operations.ast.ManualRefactoringOperation.RefactoringKind;
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


	public static ManualRefactoringOperation handleASTOperation(ASTOperation operation) {
		RefactoringProperty refactoringProperty= RefactoringPropertiesFactory.createProperty(operation);
		if (refactoringProperty != null) {
			//TODO: Decide whether a refactoring property can be a (tentative) part of several refactorings 
			//and how this should be handled, when one of the refactorings becomes complete. Apparently, a property
			//could be a part of refactorings of different kind. It is not clear if this is true for several refactorings
			//of the same kind.
			boolean wasAdded= false;
			for (ExtractVariableRefactoring refactoring : currentRefactorings) {
				if (refactoring.canBePart(refactoringProperty)) {
					refactoring.addProperty(refactoringProperty);
					if (refactoring.isComplete()) {
						//TODO: Decide what to do with shared properties, if allowed.
						currentRefactorings.remove(refactoring);
						return new ManualRefactoringOperation(refactoringKind, refactoringID++, refactoring.getArguments(), operation.getTime());
					}
					wasAdded= true;
				}
			}
			if (!wasAdded) {
				ExtractVariableRefactoring newRefactoring= new ExtractVariableRefactoring();
				if (newRefactoring.canBePart(refactoringProperty)) {
					newRefactoring.addProperty(refactoringProperty);
					currentRefactorings.add(newRefactoring);
				}
			}
		}
		return null;
	}

}
