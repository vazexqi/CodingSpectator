/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.operations.ast.InferredRefactoringOperation;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties.CorrectiveRefactoringProperty;
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

	private static final Set<RefactoringProperty> currentProperties= new HashSet<RefactoringProperty>();

	private static InferredRefactoringOperation refactoringOperation= null;


	/**
	 * Should be called before processing a sequence.
	 */
	public static void resetCurrentState() {
		refactoringID= 1;
		currentRefactorings.clear();
		currentProperties.clear();
	}

	public static InferredRefactoringOperation handleASTOperation(ASTOperation operation) {
		refactoringOperation= null;
		Set<RefactoringProperty> newProperties= RefactoringPropertiesFactory.retrieveProperties(operation);
		removeOldCurrentProperties(newProperties, operation.getTime());
		Set<RefactoringProperty> correctedProperties= collectCorrectedProperties(newProperties);

		//First, handle the corrected properties.
		for (RefactoringProperty correctedProperty : correctedProperties) {
			//This might create duplicated refactorings, so check for them.
			handleCompleteRefactoring(addPropertyToCurrentRefactorings(correctedProperty, false), operation);
		}
		//Next, handle new properties, which at this point do not include corrective properties.
		for (RefactoringProperty newProperty : newProperties) {
			handleCompleteRefactoring(addPropertyToCurrentRefactorings(newProperty, true), operation);
			addNewProperty(newProperty);
		}
		return refactoringOperation;
	}

	private static void removeOldCurrentProperties(Set<RefactoringProperty> newProperties, long currentTimestamp) {
		if (newProperties.size() > 0) { //It makes sense to remove the current properties only when there are new properties.
			//Use a temporary collection, since the properties might be removed from the main collection.
			Set<RefactoringProperty> properties= new HashSet<RefactoringProperty>();
			properties.addAll(currentProperties);
			for (RefactoringProperty property : properties) {
				property.checkTimeout(currentTimestamp);
			}
		}
	}

	private static void addNewProperty(RefactoringProperty newProperty) {
		if (newProperty.isActive()) {
			currentProperties.add(newProperty);
			if (ExtractVariableRefactoring.isAcceptableProperty(newProperty)) {
				currentRefactorings.add(ExtractVariableRefactoring.createRefactoring(newProperty));
			}
			if (InlineVariableRefactoring.isAcceptableProperty(newProperty)) {
				currentRefactorings.add(InlineVariableRefactoring.createRefactoring(newProperty));
			}
			if (RenameVariableRefactoring.isAcceptableProperty(newProperty)) {
				currentRefactorings.add(RenameVariableRefactoring.createRefactoring(newProperty));
			}
			if (RenameFieldRefactoring.isAcceptableProperty(newProperty)) {
				currentRefactorings.add(RenameFieldRefactoring.createRefactoring(newProperty));
			}
			if (RenameMethodRefactoring.isAcceptableProperty(newProperty)) {
				currentRefactorings.add(RenameMethodRefactoring.createRefactoring(newProperty));
			}
			if (RenameClassRefactoring.isAcceptableProperty(newProperty)) {
				currentRefactorings.add(RenameClassRefactoring.createRefactoring(newProperty));
			}
			if (PromoteTempRefactoring.isAcceptableProperty(newProperty)) {
				currentRefactorings.add(PromoteTempRefactoring.createRefactoring(newProperty));
			}
			if (ExtractConstantRefactoring.isAcceptableProperty(newProperty)) {
				currentRefactorings.add(ExtractConstantRefactoring.createRefactoring(newProperty));
			}
			if (ExtractMethodRefactoring.isAcceptableProperty(newProperty)) {
				currentRefactorings.add(ExtractMethodRefactoring.createRefactoring(newProperty));
			}
			if (EncapsulateFieldRefactoring.isAcceptableProperty(newProperty)) {
				currentRefactorings.add(EncapsulateFieldRefactoring.createRefactoring(newProperty));
			}
		}
	}

	private static void handleCompleteRefactoring(InferredRefactoring completeRefactoring, ASTOperation operation) {
		if (completeRefactoring != null) {
			if (refactoringOperation != null) {
				throw new RuntimeException("Already have a handled complete refactoring!");
			}
			refactoringOperation= new InferredRefactoringOperation(completeRefactoring.getKind(), refactoringID++, completeRefactoring.getArguments(), operation.getTime());

			//TODO: After disabling some properties, there could remain duplicated refactorings in currentRefactorings, i.e.,
			//refactorings with exactly the same set of properties, which potentially could lead to some performance overhead.
			//At the same time, checking for such duplicates may be an even bigger overhead.
			completeRefactoring.disableProperties();
		}
	}

	/**
	 * Has a side effect - this method removes instances of CorrectiveRefactoringProperty from the
	 * given refactoringProperties.
	 * 
	 * @param newProperties
	 * @return
	 */
	private static Set<RefactoringProperty> collectCorrectedProperties(Set<RefactoringProperty> newProperties) {
		Set<RefactoringProperty> correctedProperties= new HashSet<RefactoringProperty>();
		Iterator<RefactoringProperty> newPropertiesIterator= newProperties.iterator();
		while (newPropertiesIterator.hasNext()) {
			RefactoringProperty newProperty= newPropertiesIterator.next();
			if (newProperty instanceof CorrectiveRefactoringProperty) {
				newPropertiesIterator.remove();
				correctProperties((CorrectiveRefactoringProperty)newProperty, correctedProperties);
			}
		}
		//Fire corrected properties only after all properties are corrected 
		//to avoid destroying partially corrected refactorings.
		for (RefactoringProperty correctedProperty : correctedProperties) {
			correctedProperty.fireCorrected();
		}
		return correctedProperties;
	}

	private static void correctProperties(CorrectiveRefactoringProperty correctiveProperty, Set<RefactoringProperty> correctedProperties) {
		for (RefactoringProperty currentProperty : currentProperties) {
			if (correctiveProperty.doesMatch(currentProperty) && correctiveProperty.doesOverlap(currentProperty)) {
				correctiveProperty.correct(currentProperty);
				correctedProperties.add(currentProperty);
			}
		}
	}

	private static InferredRefactoring addPropertyToCurrentRefactorings(RefactoringProperty refactoringProperty, boolean noCheckForDuplicates) {
		Set<InferredRefactoring> newRefactorings= new HashSet<InferredRefactoring>();
		for (InferredRefactoring refactoring : currentRefactorings) {
			if (refactoring.canBePart(refactoringProperty)) {
				InferredRefactoring newRefactoring= refactoring.addProperty(refactoringProperty);
				if (noCheckForDuplicates || !isExistingRefactoring(newRefactoring)) {
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

	private static boolean isExistingRefactoring(InferredRefactoring refactoring) {
		for (InferredRefactoring currentRefactoring : currentRefactorings) {
			if (currentRefactoring.equals(refactoring)) {
				return true;
			}
		}
		return false;
	}

	public static void disabledProperty(RefactoringProperty property) {
		currentProperties.remove(property);
	}

	public static void destroyedRefactoring(InferredRefactoring refactoring) {
		currentRefactorings.remove(refactoring);
	}

}
