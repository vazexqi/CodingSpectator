/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers.ast.refactoring;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.ast.InferredRefactoringOperation;
import edu.illinois.codingtracker.operations.ast.InferredRefactoringOperation.RefactoringKind;
import edu.illinois.codingtracker.operations.refactorings.FinishedRefactoringOperation;


/**
 * This analyzer calculates for each kind of refactorings that we infer, how many manual and
 * automated refactorings were performed.
 * 
 * @author Stas Negara
 * 
 */
public class RefactoringCounterAnalyzer extends InferredRefactoringAnalyzer {

	private final Map<RefactoringKind, RefactoringCounter> refactorings= new HashMap<RefactoringKind, RefactoringCounter>();

	private final Map<RefactoringKind, RefactoringCounter> totalRefactorings= new HashMap<RefactoringKind, RefactoringCounter>();

	private final Map<RefactoringKind, RefactoringCounter> participantRefactorings= new HashMap<RefactoringKind, RefactoringCounter>();

	private final Map<RefactoringKind, ManualVSAutomatedCategoryCounter> totalAutomationProportion= new HashMap<RefactoringKind, ManualVSAutomatedCategoryCounter>();


	@Override
	protected String getTableHeader() {
		return "username,workspace ID,version,refactoring kind,manual count,automated count\n";
	}

	@Override
	protected void postprocessOperation(UserOperation userOperation) {
		if (userOperation instanceof InferredRefactoringOperation) {
			handleInferredRefactoring((InferredRefactoringOperation)userOperation);
		} else if (userOperation instanceof FinishedRefactoringOperation) {
			handleFinishedRefactoring((FinishedRefactoringOperation)userOperation);
		}
	}

	private void handleInferredRefactoring(InferredRefactoringOperation inferredRefactoring) {
		if (!shouldIgnoreInferredRefactoring(inferredRefactoring)) {
			incrementManualCounter(inferredRefactoring.getRefactoringKind());
		}
	}

	private void handleFinishedRefactoring(FinishedRefactoringOperation finishedRefactoring) {
		if (!shouldIgnoreAutomatedRefactoring(finishedRefactoring)) {
			incrementAutomatedCounter(getCurrentAutomatedRefactoringKind());
		}
	}

	private void incrementAutomatedCounter(RefactoringKind refactoringKind) {
		getRefactoringCounter(refactoringKind).automatedRefactoringCount++;
	}

	private void incrementManualCounter(RefactoringKind refactoringKind) {
		getRefactoringCounter(refactoringKind).manualRefactoringCount++;
	}

	private RefactoringCounter getRefactoringCounter(RefactoringKind refactoringKind) {
		RefactoringCounter refactoringCounter= refactorings.get(refactoringKind);
		if (refactoringCounter == null) {
			refactoringCounter= new RefactoringCounter();
			refactorings.put(refactoringKind, refactoringCounter);
		}
		return refactoringCounter;
	}

	@Override
	protected void initialize() {
		super.initialize();
		refactorings.clear();
	}

	@Override
	protected void populateResults() {
		for (Entry<RefactoringKind, RefactoringCounter> entry : refactorings.entrySet()) {
			updateGlobalCounters(participantRefactorings, entry);
			updateGlobalCounters(totalRefactorings, entry);
			appendCSVEntry(postprocessedUsername, postprocessedWorkspaceID, postprocessedVersion, entry.getKey().name(),
					entry.getValue().manualRefactoringCount, entry.getValue().automatedRefactoringCount);
		}
	}

	private void updateGlobalCounters(Map<RefactoringKind, RefactoringCounter> globalCounters, Entry<RefactoringKind, RefactoringCounter> entry) {
		RefactoringCounter globalCounter= globalCounters.get(entry.getKey());
		if (globalCounter == null) {
			globalCounter= new RefactoringCounter();
			globalCounters.put(entry.getKey(), globalCounter);
		}
		globalCounter.manualRefactoringCount+= entry.getValue().manualRefactoringCount;
		globalCounter.automatedRefactoringCount+= entry.getValue().automatedRefactoringCount;
	}

	@Override
	protected void finishedProcessingAllSequences() {
		System.out.println("Total counts:");
		for (Entry<RefactoringKind, RefactoringCounter> entry : totalRefactorings.entrySet()) {
			System.out.println(entry.getKey() + "," + entry.getValue().manualRefactoringCount + "," + entry.getValue().automatedRefactoringCount);
		}
		System.out.println("Automation categories counts:");
		for (Entry<RefactoringKind, ManualVSAutomatedCategoryCounter> entry : totalAutomationProportion.entrySet()) {
			System.out.println(entry.getKey() + "," + entry.getValue().zeroAutomation + "," + entry.getValue().oneQuarterAutomation +
					"," + entry.getValue().twoQuartersAutomation + "," + entry.getValue().threeQuartersAutomation +
					"," + entry.getValue().fourQuartersAutomation + "," + entry.getValue().allAutomation);
		}
	}

	@Override
	protected void finishedProcessingParticipant() {
		for (Entry<RefactoringKind, RefactoringCounter> entry : participantRefactorings.entrySet()) {
			ManualVSAutomatedCategoryCounter manualVSAutomatedCategoryCounter= totalAutomationProportion.get(entry.getKey());
			if (manualVSAutomatedCategoryCounter == null) {
				manualVSAutomatedCategoryCounter= new ManualVSAutomatedCategoryCounter();
				totalAutomationProportion.put(entry.getKey(), manualVSAutomatedCategoryCounter);
			}
			manualVSAutomatedCategoryCounter.updateCounter(entry.getValue().manualRefactoringCount, entry.getValue().automatedRefactoringCount);
		}
		participantRefactorings.clear();
	}

	@Override
	protected String getResultFilePostfix() {
		return ".refactoring_counts";
	}

	private class RefactoringCounter {

		private int manualRefactoringCount= 0;

		private int automatedRefactoringCount= 0;

	}

	private class ManualVSAutomatedCategoryCounter {

		private int zeroAutomation= 0; // 0% refactorings performed automatically.

		private int oneQuarterAutomation= 0; // (0%, 25%] refactorings performed automatically.

		private int twoQuartersAutomation= 0; // (25%, 50%] refactorings performed automatically.

		private int threeQuartersAutomation= 0; // (50%, 75%] refactorings performed automatically.

		private int fourQuartersAutomation= 0; // (75%, 100%) refactorings performed automatically.

		private int allAutomation= 0; // 100% refactorings performed automatically.


		public void updateCounter(int manualRefactoringCount, int automatedRefactoringCount) {
			//It is double to avoid unnecessary rounding.
			double totalRefactoringCount= manualRefactoringCount + automatedRefactoringCount;
			if (totalRefactoringCount > 0) {
				if (automatedRefactoringCount == 0) {
					zeroAutomation++;
				} else if (manualRefactoringCount == 0) {
					allAutomation++;
				} else if (automatedRefactoringCount / totalRefactoringCount <= 0.25) {
					oneQuarterAutomation++;
				} else if (automatedRefactoringCount / totalRefactoringCount <= 0.5) {
					twoQuartersAutomation++;
				} else if (automatedRefactoringCount / totalRefactoringCount <= 0.75) {
					threeQuartersAutomation++;
				} else {
					fourQuartersAutomation++;
				}
			}
		}

	}

}
