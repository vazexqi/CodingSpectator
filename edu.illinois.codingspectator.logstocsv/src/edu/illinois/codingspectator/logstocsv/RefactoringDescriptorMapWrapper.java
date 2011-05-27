/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.logstocsv;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import edu.illinois.codingspectator.refactorings.parser.CapturedRefactoringDescriptor;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class RefactoringDescriptorMapWrapper {

	private CapturedRefactoringDescriptor capturedRefactoringDescriptor;

	private String username;

	private String workspaceID;

	private String codingspectatorVersion;

	private String refactoringKind;

	public RefactoringDescriptorMapWrapper(CapturedRefactoringDescriptor capturedRefactoringDescriptor, String username, String workspaceID, String codingspectatorVersion, String refactoringKind) {
		this.capturedRefactoringDescriptor= capturedRefactoringDescriptor;
		this.username= username;
		this.workspaceID= workspaceID;
		this.codingspectatorVersion= codingspectatorVersion;
		this.refactoringKind= refactoringKind;
	}

	public Map<String, String> toMap() {
		Map<String, String> map= new HashMap<String, String>();
		map.put("comment", capturedRefactoringDescriptor.getComment());
		map.put("description", capturedRefactoringDescriptor.getDescription());
		map.put("flags", String.valueOf(capturedRefactoringDescriptor.getFlags()));
		map.put("id", capturedRefactoringDescriptor.getID());
		map.put("project", capturedRefactoringDescriptor.getProject());
		map.put("timestamp", String.valueOf(capturedRefactoringDescriptor.getTimestamp()));
		map.put("human-readable timestamp", new Date(capturedRefactoringDescriptor.getTimestamp()).toString());
		map.putAll(capturedRefactoringDescriptor.getArguments());
		map.put("username", username);
		map.put("workspace ID", workspaceID);
		map.put("codingspectator version", codingspectatorVersion);
		map.put("refactoring kind", refactoringKind);
		map.put("severity level", String.valueOf(getSeverityLevel(capturedRefactoringDescriptor.getAttribute("status"))));
		return map;
	}

	private int getSeverityLevel(String status) {
		if (status.contains("OK")) {
			return 0;
		} else if (status.contains("INFO")) {
			return 1;
		} else if (status.contains("WARNING")) {
			return 2;
		} else if (status.contains("FATALERROR")) {
			return 4;
		} else if (status.contains("ERROR")) {
			return 3;
		}
		return 5;
	}
}
