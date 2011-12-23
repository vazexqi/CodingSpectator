/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.logstocsv;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.files.UpdatedFileOperation;
import edu.illinois.codingtracker.operations.files.snapshoted.CVSCommittedFileOperation;
import edu.illinois.codingtracker.operations.files.snapshoted.CVSInitiallyCommittedFileOperation;
import edu.illinois.codingtracker.operations.files.snapshoted.SVNCommittedFileOperation;
import edu.illinois.codingtracker.operations.files.snapshoted.SVNInitiallyCommittedFileOperation;
import edu.illinois.codingtracker.operations.junit.TestSessionStartedOperation;
import edu.illinois.codingtracker.operations.refactorings.FinishedRefactoringOperation;
import edu.illinois.codingtracker.operations.refactorings.NewStartedRefactoringOperation;
import edu.illinois.codingtracker.operations.refactorings.NewStartedRefactoringOperation.RefactoringMode;
import edu.illinois.codingtracker.operations.refactorings.PerformedRefactoringOperation;
import edu.illinois.codingtracker.operations.refactorings.RedoneRefactoringOperation;
import edu.illinois.codingtracker.operations.refactorings.RefactoringOperation;
import edu.illinois.codingtracker.operations.refactorings.UndoneRefactoringOperation;
import edu.illinois.codingtracker.operations.starts.LaunchedApplicationOperation;

/**
 * 
 * @author Mohsen Vakilian
 * 
 */
public class UserOperationEvent extends Event {

	@SuppressWarnings("unchecked")
	private static List<Class<? extends UserOperation>> shouldBeIncludedInCSV= Arrays.asList(TestSessionStartedOperation.class, LaunchedApplicationOperation.class,
			PerformedRefactoringOperation.class, RedoneRefactoringOperation.class, UndoneRefactoringOperation.class, NewStartedRefactoringOperation.class, FinishedRefactoringOperation.class,
			CVSCommittedFileOperation.class, CVSInitiallyCommittedFileOperation.class, SVNCommittedFileOperation.class, SVNInitiallyCommittedFileOperation.class, UpdatedFileOperation.class);

	private UserOperation userOperation;

	public UserOperationEvent(UserOperation userOperation, String username, String workspaceID, String codingSpectatorVersion) {
		super(username, workspaceID, codingSpectatorVersion);
		this.userOperation= userOperation;
	}

	@Override
	public Map<String, String> toMap() {
		Map<String, String> map= super.toMap();
		map.put("codingtracker description", String.valueOf(getDescription()));
		map.put("timestamp", String.valueOf(getTimestamp()));
		Date timestampDate= userOperation.getDate();
		map.put("human-readable timestamp", timestampDate.toString());;
		map.put("recorder", "CODINGTRACKER");
		if (isNewStartedRefactoringOperation()) {
			addNewStartedRefactoringInformation(map);
		} else if (userOperation instanceof FinishedRefactoringOperation) {
			map.put("success", String.valueOf(((FinishedRefactoringOperation)userOperation).getSuccess()));
		} else if (isRefactoringOperation()) {
			addRefactoringOperationInformation(map);
		}
		return map;
	}

	private boolean isRefactoringOperation() {
		return userOperation instanceof RefactoringOperation;
	}

	private boolean isNewStartedRefactoringOperation() {
		return userOperation instanceof NewStartedRefactoringOperation;
	}

	public boolean isStartedPerformedRefactoringOperation() {
		return isNewStartedRefactoringOperation() && ((NewStartedRefactoringOperation)userOperation).getRefactoringMode() == RefactoringMode.PERFORM;
	}

	private void addNewStartedRefactoringInformation(Map<String, String> map) {
		NewStartedRefactoringOperation startedRefactoringOperation= (NewStartedRefactoringOperation)userOperation;
		map.put("refactoring kind", getNewStartedRefactoringKind());
		map.put("flags", String.valueOf(startedRefactoringOperation.getFlags()));
		map.put("id", startedRefactoringOperation.getID());
		map.put("project", startedRefactoringOperation.getProject());
	}

	private void addRefactoringOperationInformation(Map<String, String> map) {
		RefactoringOperation refactoringOperation= (RefactoringOperation)userOperation;
		map.put("refactoring kind", getRefactoringOperationKind());
		map.put("flags", String.valueOf(refactoringOperation.getFlags()));
		map.put("id", refactoringOperation.getID());
		map.put("project", refactoringOperation.getProject());
	}

	private String getNewStartedRefactoringKind() {
		NewStartedRefactoringOperation startedRefactoringOperation= (NewStartedRefactoringOperation)userOperation;

		switch (startedRefactoringOperation.getRefactoringMode()) {
			case PERFORM:
				return "PERFORMED";
			case UNDO:
				return "UNDONE";
			case REDO:
				return "REDONE";
			default:
				throw new IllegalArgumentException();
		}
	}

	private String getRefactoringOperationKind() {
		if (userOperation instanceof PerformedRefactoringOperation) {
			return "PERFORMED";
		} else if (userOperation instanceof UndoneRefactoringOperation) {
			return "UNDONE";
		} else if (userOperation instanceof RedoneRefactoringOperation) {
			return "REDONE";
		} else {
			throw new IllegalArgumentException();
		}
	}

	public String getDescription() {
		return userOperation.getDescription();
	}

	@Override
	public long getTimestamp() {
		return userOperation.getTime();
	}

	public boolean shouldBeIncludedInCSV() {
		return shouldBeIncludedInCSV.contains(userOperation.getClass());
	}

}
