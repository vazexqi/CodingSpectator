/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.logstocsv;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import edu.illinois.codingspectator.refactorings.parser.CapturedRefactoringDescriptor;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class RefactoringDescriptorMapWrapper extends AbstractMapWrapper {

	private CapturedRefactoringDescriptor capturedRefactoringDescriptor;

	private String refactoringKind;

	public RefactoringDescriptorMapWrapper(CapturedRefactoringDescriptor capturedRefactoringDescriptor, String username, String workspaceID, String codingspectatorVersion, String refactoringKind) {
		super(username, workspaceID, codingspectatorVersion);
		this.capturedRefactoringDescriptor= capturedRefactoringDescriptor;
		this.refactoringKind= refactoringKind;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map<String, String> toMap() {
		Map<String, String> map= super.toMap();
		map.put("comment", capturedRefactoringDescriptor.getComment());
		map.put("description", capturedRefactoringDescriptor.getDescription());
		map.put("flags", String.valueOf(capturedRefactoringDescriptor.getFlags()));
		map.put("id", capturedRefactoringDescriptor.getID());
		map.put("project", capturedRefactoringDescriptor.getProject());
		map.put("timestamp", String.valueOf(capturedRefactoringDescriptor.getTimestamp()));
		Date timestampDate= new Date(capturedRefactoringDescriptor.getTimestamp());
		map.put("human-readable timestamp", timestampDate.toString());
		SimpleDateFormat tableauDateFormat= new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		map.put("Tableau timestamp", tableauDateFormat.format(timestampDate));
		map.putAll(capturedRefactoringDescriptor.getArguments());
		map.put("refactoring kind", refactoringKind);
		map.put("severity level", String.valueOf(getSeverityLevel(capturedRefactoringDescriptor.getAttribute("status"))));
		return map;
	}

	private int getSeverityLevel(String status) {
		if (status == null) {
			return 0;
		}
		if (status.contains("OK")) {
			return 1;
		} else if (status.contains("INFO")) {
			return 2;
		} else if (status.contains("WARNING")) {
			return 3;
		} else if (status.contains("FATALERROR")) {
			return 5;
		} else if (status.contains("ERROR")) {
			return 4;
		}
		return 6;
	}
}
