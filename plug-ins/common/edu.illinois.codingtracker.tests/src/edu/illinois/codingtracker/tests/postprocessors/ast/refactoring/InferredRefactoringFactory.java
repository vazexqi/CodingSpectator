/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.postprocessors.ast.refactoring;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.operations.ast.InferredRefactoringOperation;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties.AddedGetterMethodInvocationRefactoringProperty;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties.AtomicRefactoringProperty;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties.CorrectiveRefactoringProperty;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties.DeletedEntityReferenceRefactoringProperty;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties.MovedToUsageRefactoringProperty;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties.RefactoringPropertiesFactory;
import edu.illinois.codingtracker.tests.postprocessors.ast.refactoring.properties.RefactoringProperty;



/**
 * This class handles instances of the inferred refactorings.
 * 
 * @author Stas Negara
 * 
 */
public class InferredRefactoringFactory {

	public static List<UserOperation> userOperations;

	private static long refactoringID= 1;

	//List is chosen over Set to simplify checking of the results using the assigned refactoring IDs.
	private static final List<InferredRefactoring> completeRefactorings= new LinkedList<InferredRefactoring>();

	private static final Set<InferredRefactoring> pendingRefactorings= new HashSet<InferredRefactoring>();

	private static final Set<InferredRefactoringFragment> pendingFragments= new HashSet<InferredRefactoringFragment>();

	private static final Set<RefactoringProperty> currentProperties= new HashSet<RefactoringProperty>();


	/**
	 * Should be called before processing a sequence.
	 */
	public static void resetCurrentState() {
		refactoringID= 1;
		completeRefactorings.clear();
		pendingRefactorings.clear();
		pendingFragments.clear();
		currentProperties.clear();
	}

	public static void handleASTOperation(ASTOperation operation) {
		addPossiblyRelatedOperationToCurrentPropertiesAndCompleteRefactorings(operation);
		long currentTimestamp= operation.getTime();
		removeOldCompleteRefactorings(currentTimestamp);
		Set<RefactoringProperty> newProperties= RefactoringPropertiesFactory.retrieveProperties(operation);
		applyFilteringHeuritics(newProperties);
		removeOldCurrentProperties(newProperties, currentTimestamp);
		Set<RefactoringProperty> correctedProperties= collectCorrectedProperties(newProperties);

		Set<RefactoringProperty> propertiesToAdd= new HashSet<RefactoringProperty>();
		propertiesToAdd.addAll(newProperties);
		//First, process refactoring fragments, since doing so may lead to additional properties to be added to refactorings.
		processPendingRefactoringFragments(propertiesToAdd, correctedProperties);
		processPendingRefactorings(propertiesToAdd, correctedProperties);
	}

	private static void applyFilteringHeuritics(Set<RefactoringProperty> properties) {
		if (!RefactoringInferencePostprocessor.isIntroducingGetterInvocation) {
			return;
		}
		Iterator<RefactoringProperty> propertiesIterator= properties.iterator();
		while (propertiesIterator.hasNext()) {
			RefactoringProperty property= propertiesIterator.next();
			if (!(property instanceof AddedGetterMethodInvocationRefactoringProperty) &&
					!(property instanceof DeletedEntityReferenceRefactoringProperty) &&
					!(property instanceof MovedToUsageRefactoringProperty)) {
				propertiesIterator.remove();
			}
		}
	}

	private static void addPossiblyRelatedOperationToCurrentPropertiesAndCompleteRefactorings(ASTOperation operation) {
		for (RefactoringProperty currentProperty : currentProperties) {
			currentProperty.addPossiblyRelatedOperation(operation);
		}
		for (int i= completeRefactorings.size() - 1; i >= 0; i--) { //Start with the most recent completed refactoring.
			if (completeRefactorings.get(i).addPossiblyRelatedOperation(operation)) {
				//Can add to a single completed refactoring only.
				break;
			}
		}
	}

	private static void processPendingRefactoringFragments(Set<RefactoringProperty> propertiesToAdd, Set<RefactoringProperty> correctedProperties) {
		//First, handle the corrected properties.
		for (RefactoringProperty correctedProperty : correctedProperties) {
			propertiesToAdd.addAll(addPropertyToPendingRefactoringFragments(correctedProperty));
		}
		//Next, handle the properties to add.
		Set<RefactoringProperty> newPropertiesToAdd= new HashSet<RefactoringProperty>();
		for (RefactoringProperty propertyToAdd : propertiesToAdd) {
			newPropertiesToAdd.addAll(addPropertyToPendingRefactoringFragments(propertyToAdd));
			addNewFragmentProperty(propertyToAdd);
		}
		propertiesToAdd.addAll(newPropertiesToAdd);
	}

	private static void processPendingRefactorings(Set<RefactoringProperty> propertiesToAdd, Set<RefactoringProperty> correctedProperties) {
		//First, handle the corrected properties.
		for (RefactoringProperty correctedProperty : correctedProperties) {
			//This might create duplicated refactorings, so check for them.
			addPropertyToPendingRefactorings(correctedProperty, false);
		}
		//Next, handle the properties to add.
		for (RefactoringProperty propertyToAdd : propertiesToAdd) {
			if (!addPropertyToCompleteRefactorings(propertyToAdd)) {
				//Add a new property to pending refactorings only if it was not added to a complete refactoring.
				addPropertyToPendingRefactorings(propertyToAdd, true);
				addNewRefactoringProperty(propertyToAdd);
			}
		}
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

	private static void removeOldCompleteRefactorings(long currentTimestamp) {
		Iterator<InferredRefactoring> completeRefactoringsIterator= completeRefactorings.iterator();
		while (completeRefactoringsIterator.hasNext()) {
			InferredRefactoring completeRefactoring= completeRefactoringsIterator.next();
			if (completeRefactoring.isOld(currentTimestamp)) {
				insertInferredRefactoring(completeRefactoring);
				completeRefactoringsIterator.remove();
			}
		}
	}

	//At the end of sequence processing, even if a complete refactoring is not old, it should be inserted.
	public static void flushCompleteRefactorings() {
		for (InferredRefactoring inferredRefactoring : completeRefactorings) {
			insertInferredRefactoring(inferredRefactoring);
		}
		completeRefactorings.clear();
	}

	private static void insertInferredRefactoring(InferredRefactoring inferredRefactoring) {
		inferredRefactoring.setRefactoringID(refactoringID);
		ASTOperation lastContributingOperation= inferredRefactoring.getLastContributingOperation();
		InferredRefactoringOperation refactoringOperation= new InferredRefactoringOperation(inferredRefactoring.getKind(), refactoringID, inferredRefactoring.getArguments(),
				lastContributingOperation.getTime());
		int insertIndex= userOperations.indexOf(lastContributingOperation) + 1;
		userOperations.add(insertIndex, refactoringOperation);
		refactoringID++;
	}

	private static void addNewRefactoringProperty(RefactoringProperty newProperty) {
		if (newProperty.isActive()) {
			currentProperties.add(newProperty);
			if (ExtractVariableRefactoring.isAcceptableProperty(newProperty)) {
				pendingRefactorings.add(ExtractVariableRefactoring.createRefactoring(newProperty));
			}
			if (InlineVariableRefactoring.isAcceptableProperty(newProperty)) {
				pendingRefactorings.add(InlineVariableRefactoring.createRefactoring(newProperty));
			}
			if (RenameVariableRefactoring.isAcceptableProperty(newProperty)) {
				pendingRefactorings.add(RenameVariableRefactoring.createRefactoring(newProperty));
			}
			if (RenameFieldRefactoring.isAcceptableProperty(newProperty)) {
				pendingRefactorings.add(RenameFieldRefactoring.createRefactoring(newProperty));
			}
			if (RenameMethodRefactoring.isAcceptableProperty(newProperty)) {
				pendingRefactorings.add(RenameMethodRefactoring.createRefactoring(newProperty));
			}
			if (RenameClassRefactoring.isAcceptableProperty(newProperty)) {
				pendingRefactorings.add(RenameClassRefactoring.createRefactoring(newProperty));
			}
			if (PromoteTempRefactoring.isAcceptableProperty(newProperty)) {
				pendingRefactorings.add(PromoteTempRefactoring.createRefactoring(newProperty));
			}
			if (ExtractConstantRefactoring.isAcceptableProperty(newProperty)) {
				pendingRefactorings.add(ExtractConstantRefactoring.createRefactoring(newProperty));
			}
			if (ExtractMethodRefactoring.isAcceptableProperty(newProperty)) {
				pendingRefactorings.add(ExtractMethodRefactoring.createRefactoring(newProperty));
			}
			if (EncapsulateFieldRefactoring.isAcceptableProperty(newProperty)) {
				pendingRefactorings.add(EncapsulateFieldRefactoring.createRefactoring(newProperty));
			}
		}
	}

	private static void addNewFragmentProperty(RefactoringProperty newProperty) {
		if (newProperty.isActive()) {
			if (ReplacedEntityWithExpressionRefactoringFragment.isAcceptableProperty(newProperty)) {
				pendingFragments.add(ReplacedEntityWithExpressionRefactoringFragment.createRefactoring(newProperty));
			}
			if (ReplacedExpressionWithEntityRefactoringFragment.isAcceptableProperty(newProperty)) {
				pendingFragments.add(ReplacedExpressionWithEntityRefactoringFragment.createRefactoring(newProperty));
			}
			if (ReplacedEntityWithGetterRefactoringFragment.isAcceptableProperty(newProperty)) {
				pendingFragments.add(ReplacedEntityWithGetterRefactoringFragment.createRefactoring(newProperty));
			}
			if (ReplacedEntityWithSetterRefactoringFragment.isAcceptableProperty(newProperty)) {
				pendingFragments.add(ReplacedEntityWithSetterRefactoringFragment.createRefactoring(newProperty));
			}
			if (MovedAcrossMethodsRefactoringFragment.isAcceptableProperty(newProperty)) {
				pendingFragments.add(MovedAcrossMethodsRefactoringFragment.createRefactoring(newProperty));
			}
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
			if (currentProperty instanceof AtomicRefactoringProperty) { //Only atomic properties can be corrected.
				AtomicRefactoringProperty atomicProperty= (AtomicRefactoringProperty)currentProperty;
				if (correctiveProperty.doesMatch(null, atomicProperty) && correctiveProperty.doesOverlap(atomicProperty)) {
					correctiveProperty.correct(atomicProperty);
					correctedProperties.add(atomicProperty);
				}
			}
		}
	}

	private static boolean addPropertyToCompleteRefactorings(RefactoringProperty refactoringProperty) {
		for (InferredRefactoring completeRefactoring : completeRefactorings) {
			if (completeRefactoring.canBePart(refactoringProperty) && !completeRefactoring.doesAffectSameEntity(refactoringProperty)) {
				completeRefactoring.addProperty(refactoringProperty, false);
				refactoringProperty.disable();
				return true;
			}
		}
		return false;
	}

	private static void addPropertyToPendingRefactorings(RefactoringProperty refactoringProperty, boolean noCheckForDuplicates) {
		Set<InferredRefactoring> newRefactorings= new HashSet<InferredRefactoring>();
		Set<InferredRefactoring> newCompleteRefactorings= new HashSet<InferredRefactoring>();
		collectNewAndCompleteRefactorings(refactoringProperty, noCheckForDuplicates, newRefactorings, newCompleteRefactorings);
		InferredRefactoring largestCompleteRefactoring= pickLargestRefactoring(newCompleteRefactorings);
		if (largestCompleteRefactoring != null) {
			insertInferredRefactoringsConflictingWith(largestCompleteRefactoring);
			completeRefactorings.add(largestCompleteRefactoring);
			//TODO: After disabling some properties, there could remain duplicated refactorings in currentRefactorings, i.e.,
			//refactorings with exactly the same set of properties, which potentially could lead to some performance overhead.
			//At the same time, checking for such duplicates may be an even bigger overhead.
			largestCompleteRefactoring.disableProperties();
		} else {
			pendingRefactorings.addAll(newRefactorings);
		}
	}

	private static void insertInferredRefactoringsConflictingWith(InferredRefactoring newCompleteRefactoring) {
		Iterator<InferredRefactoring> completeRefactoringsIterator= completeRefactorings.iterator();
		while (completeRefactoringsIterator.hasNext()) {
			InferredRefactoring completeRefactoring= completeRefactoringsIterator.next();
			if (completeRefactoring.doesAffectSameEntity(newCompleteRefactoring)) {
				insertInferredRefactoring(completeRefactoring);
				completeRefactoringsIterator.remove();
			}
		}
	}

	private static void collectNewAndCompleteRefactorings(RefactoringProperty refactoringProperty, boolean noCheckForDuplicates,
															Set<InferredRefactoring> newRefactorings, Set<InferredRefactoring> newCompleteRefactorings) {
		for (InferredRefactoring refactoring : pendingRefactorings) {
			if (refactoring.canBePart(refactoringProperty)) {
				InferredRefactoring newRefactoring;
				if (refactoring.hasMultiProperty(refactoringProperty.getClassName())) {
					newRefactoring= refactoring.addProperty(refactoringProperty, false);
				} else {
					newRefactoring= refactoring.addProperty(refactoringProperty, true);
					if (noCheckForDuplicates || !isExistingRefactoring(newRefactoring)) {
						newRefactorings.add(newRefactoring);
					}
				}
				if (newRefactoring.isComplete()) {
					newCompleteRefactorings.add(newRefactoring);
				}
			}
		}
	}

	private static InferredRefactoring pickLargestRefactoring(Set<InferredRefactoring> refactorings) {
		InferredRefactoring largestRefactoring= null;
		int largestCount= 0;
		for (InferredRefactoring refactoring : refactorings) {
			int propertiesCount= refactoring.getPropertiesCount();
			if (propertiesCount > largestCount) {
				largestCount= propertiesCount;
				largestRefactoring= refactoring;
			}
		}
		return largestRefactoring;
	}

	private static Set<InferredRefactoringFragment> addPropertyToPendingRefactoringFragments(RefactoringProperty refactoringProperty) {
		Set<InferredRefactoringFragment> newFragments= new HashSet<InferredRefactoringFragment>();
		Set<InferredRefactoringFragment> newCompleteFragments= new HashSet<InferredRefactoringFragment>();
		for (InferredRefactoringFragment refactoringFragment : pendingFragments) {
			if (refactoringFragment.canBePart(refactoringProperty)) {
				InferredRefactoringFragment newFragment= (InferredRefactoringFragment)refactoringFragment.addProperty(refactoringProperty, true);
				if (newFragment.isComplete()) {
					newCompleteFragments.add(newFragment);
				} else {
					newFragments.add(newFragment);
				}
			}
		}
		pendingFragments.addAll(newFragments);
		return newCompleteFragments;
	}

	private static boolean isExistingRefactoring(InferredRefactoring refactoring) {
		for (InferredRefactoring currentRefactoring : pendingRefactorings) {
			if (currentRefactoring.equals(refactoring)) {
				return true;
			}
		}
		return false;
	}

	public static void disabledProperty(RefactoringProperty property) {
		currentProperties.remove(property);
		pendingFragments.remove(property);
	}

	public static void destroyedRefactoring(InferredRefactoring refactoring) {
		if (refactoring instanceof RefactoringProperty) {
			disabledProperty((RefactoringProperty)refactoring);
		} else {
			pendingRefactorings.remove(refactoring);
		}
	}

}
