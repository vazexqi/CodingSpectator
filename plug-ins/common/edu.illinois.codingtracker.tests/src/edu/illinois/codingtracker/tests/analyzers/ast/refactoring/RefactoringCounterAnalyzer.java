/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers.ast.refactoring;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.ast.InferredRefactoringOperation;
import edu.illinois.codingtracker.operations.ast.InferredRefactoringOperation.RefactoringKind;
import edu.illinois.codingtracker.operations.refactorings.FinishedRefactoringOperation;
import edu.illinois.codingtracker.operations.refactorings.NewStartedRefactoringOperation;
import edu.illinois.codingtracker.operations.refactorings.NewStartedRefactoringOperation.RefactoringMode;
import edu.illinois.codingtracker.tests.analyzers.CSVProducingAnalyzer;


/**
 * This analyzer calculates for each kind of refactorings that we infer, how many manual and
 * automated refactorings were performed.
 * 
 * @author Stas Negara
 * 
 */
public class RefactoringCounterAnalyzer extends CSVProducingAnalyzer {

	private final Map<RefactoringKind, RefactoringCounter> refactorings= new HashMap<RefactoringKind, RefactoringCounter>();


	@Override
	protected String getTableHeader() {
		return "username,workspace ID,refactoring kind,manual count,automated count\n";
	}

	@Override
	protected void checkPostprocessingPreconditions() {
		//no preconditions
	}

	@Override
	protected boolean shouldPostprocessVersionFolder(String folderName) {
		return true;
	}

	@Override
	protected String getRecordFileName() {
		return "codechanges.txt.inferred_ast_operations.with_move.with_inferred_refactorings";
	}

	@Override
	protected void postprocess(List<UserOperation> userOperations) {
		initialize();
		boolean isInsideAutomatedRefactoring= false;
		for (UserOperation userOperation : userOperations) {
			//TODO: Discard manual renames that follow automated extract refactorings (e.g., extract method, constant, etc.).
			//Should follow within a reasonable time window, and the extracted entity name should be a default value.
			//TODO: Also, discard automated renames of entities that do not have references. This is required to make
			//comparison of manual vs. automated refactorings fair, since we do not infer such renames.
			//For similar reasons, discard automated "Extract Local Variable" refactorings that are just assigning 
			//a statement's result to a newly created variable.
			if (userOperation instanceof NewStartedRefactoringOperation) {
				isInsideAutomatedRefactoring= true;
				NewStartedRefactoringOperation refactoringOperation= (NewStartedRefactoringOperation)userOperation;
				if (refactoringOperation.getRefactoringMode() == RefactoringMode.PERFORM) { //Consider only performed refactorins.
					RefactoringKind refactoringKind= getRefactoringKindForID(refactoringOperation.getID());
					if (refactoringKind != null) {
						incrementAutomatedCounter(refactoringKind);
					}
				}
			} else if (userOperation instanceof FinishedRefactoringOperation) {
				isInsideAutomatedRefactoring= false;
			} else if (userOperation instanceof InferredRefactoringOperation && !isInsideAutomatedRefactoring) {
				incrementManualCounter(((InferredRefactoringOperation)userOperation).getRefactoringKind());
			}
		}
		populateResults();
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

	private void initialize() {
		result= new StringBuffer();
		refactorings.clear();
	}

	private void populateResults() {
		for (Entry<RefactoringKind, RefactoringCounter> entry : refactorings.entrySet()) {
			appendCSVEntry(postprocessedUsername, postprocessedWorkspaceID, entry.getKey().name(), entry.getValue().manualRefactoringCount, entry.getValue().automatedRefactoringCount);
		}
	}

	@Override
	protected String getResultFilePostfix() {
		return ".refactoring_counts";
	}

	@Override
	protected boolean shouldMergeResults() {
		return true;
	}

	@Override
	protected boolean shouldOutputIndividualResults() {
		return false;
	}

	private static RefactoringKind getRefactoringKindForID(String refactoringID) {
		String refactoringName= refactoringID.substring("org.eclipse.jdt.ui.".length());
		if (refactoringName.equals("extract.temp")) {
			return RefactoringKind.EXTRACT_LOCAL_VARIABLE;
		}
		if (refactoringName.equals("inline.temp")) {
			return RefactoringKind.INLINE_LOCAL_VARIABLE;
		}
		if (refactoringName.equals("rename.local.variable")) {
			return RefactoringKind.RENAME_LOCAL_VARIABLE;
		}
		if (refactoringName.equals("rename.field")) {
			return RefactoringKind.RENAME_FIELD;
		}
		if (refactoringName.equals("rename.method")) {
			return RefactoringKind.RENAME_METHOD;
		}
		if (refactoringName.equals("rename.type")) {
			return RefactoringKind.RENAME_CLASS;
		}
		if (refactoringName.equals("promote.temp")) {
			return RefactoringKind.PROMOTE_TEMP;
		}
		if (refactoringName.equals("extract.constant")) {
			return RefactoringKind.EXTRACT_CONSTANT;
		}
		if (refactoringName.equals("extract.method")) {
			return RefactoringKind.EXTRACT_METHOD;
		}
		if (refactoringName.equals("self.encapsulate")) {
			return RefactoringKind.ENCAPSULATE_FIELD;
		}
		return null;
	}

	private class RefactoringCounter {

		private int manualRefactoringCount= 0;

		private int automatedRefactoringCount= 0;

	}

}
