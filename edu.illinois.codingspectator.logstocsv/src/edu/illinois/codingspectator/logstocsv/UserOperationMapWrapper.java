package edu.illinois.codingspectator.logstocsv;

import java.text.SimpleDateFormat;
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
import edu.illinois.codingtracker.operations.junit.TestSessionFinishedOperation;
import edu.illinois.codingtracker.operations.refactorings.RedoneRefactoringOperation;
import edu.illinois.codingtracker.operations.refactorings.UndoneRefactoringOperation;
import edu.illinois.codingtracker.operations.starts.LaunchedApplicationOperation;

public class UserOperationMapWrapper extends AbstractMapWrapper {

	@SuppressWarnings("unchecked")
	private static List<Class<? extends UserOperation>> shouldBeIncludedInCSV= Arrays.asList(TestSessionFinishedOperation.class, LaunchedApplicationOperation.class, RedoneRefactoringOperation.class,
			UndoneRefactoringOperation.class, CVSCommittedFileOperation.class, CVSInitiallyCommittedFileOperation.class, SVNCommittedFileOperation.class, SVNInitiallyCommittedFileOperation.class,
			UpdatedFileOperation.class
			);

	private UserOperation userOperation;

	public UserOperationMapWrapper(UserOperation userOperation, String username, String workspaceID, String codingSpectatorVersion) {
		super(username, workspaceID, codingSpectatorVersion);
		this.userOperation= userOperation;
	}

	@Override
	public Map<String, String> toMap() {
		Map<String, String> map= super.toMap();
		map.put("description", String.valueOf(userOperation.getDescription()));
		map.put("timestamp", String.valueOf(userOperation.getTime()));
		Date timestampDate= userOperation.getDate();
		map.put("human-readable timestamp", timestampDate.toString());
		SimpleDateFormat tableauDateFormat= new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		map.put("Tableau timestamp", tableauDateFormat.format(timestampDate));
		return map;
	}

	public boolean shouldBeIncludedInCSV() {
		return shouldBeIncludedInCSV.contains(userOperation.getClass());
	}
}
