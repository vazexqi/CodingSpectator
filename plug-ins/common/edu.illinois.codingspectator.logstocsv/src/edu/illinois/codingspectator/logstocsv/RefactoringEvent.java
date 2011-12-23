/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.logstocsv;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.ltk.core.refactoring.codingspectator.NavigationHistory;
import org.eclipse.ltk.core.refactoring.codingspectator.NavigationHistory.ParseException;
import org.eclipse.ltk.core.refactoring.codingspectator.NavigationHistoryItem;

import edu.illinois.codingspectator.refactorings.parser.CapturedRefactoringDescriptor;
import edu.illinois.codingspectator.refactorings.parser.RefactoringLog.LogType;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class RefactoringEvent extends Event {

	private CapturedRefactoringDescriptor capturedRefactoringDescriptor;

	private LogType refactoringKind;

	public RefactoringEvent(CapturedRefactoringDescriptor capturedRefactoringDescriptor, String username, String workspaceID, String codingspectatorVersion, LogType refactoringKind) {
		super(username, workspaceID, codingspectatorVersion);
		this.capturedRefactoringDescriptor= capturedRefactoringDescriptor;
		this.refactoringKind= refactoringKind;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map<String, String> toMap() {
		Map<String, String> map= super.toMap();
		String comment= capturedRefactoringDescriptor.getComment();
		map.put("comment", truncateString(comment));
		map.put("description", capturedRefactoringDescriptor.getDescription());
		map.put("flags", String.valueOf(capturedRefactoringDescriptor.getFlags()));
		map.put("id", capturedRefactoringDescriptor.getID());
		map.put("project", capturedRefactoringDescriptor.getProject());
		map.put("timestamp", String.valueOf(getTimestamp()));
		Date timestampDate= new Date(getTimestamp());
		map.put("human-readable timestamp", timestampDate.toString());
		map.putAll(capturedRefactoringDescriptor.getArguments());
		switch (getRefactoringKind()) {
			case ECLIPSE:
				map.put("refactoring kind", "PERFORMED");
				map.put("recorder", "ECLIPSE");
				break;
			case PERFORMED:
				map.put("refactoring kind", "PERFORMED");
				map.put("recorder", "CODINGSPECTATOR");
				break;
			case CANCELLED:
				map.put("refactoring kind", "CANCELLED");
				map.put("recorder", "CODINGSPECTATOR");
				break;
			case UNAVAILABLE:
				map.put("refactoring kind", "UNAVAILABLE");
				map.put("recorder", "CODINGSPECTATOR");
				break;
			default:
				break;
		}
		map.put("severity level", String.valueOf(getSeverityLevel(capturedRefactoringDescriptor.getAttribute("status"))));
		map.put("navigation duration", getNavigationDurationString(capturedRefactoringDescriptor.getAttribute("navigation-history")));
		if (capturedRefactoringDescriptor.getCodeSnippet() != null && capturedRefactoringDescriptor.getSelectionInCodeSnippet() != null) {
			map.put("code-snippet-with-selection-markers", getCodesnippetWithSelectionMarkers());
		}
		return map;
	}

	private String truncateString(String comment) {
		if (comment.length() == 0)
			return "";
		int maxLength= comment.length() > ATTRIBUTE_LENGTH_LIMIT ? ATTRIBUTE_LENGTH_LIMIT : comment.length();
		return comment.substring(0, maxLength);
	}

	@Override
	public long getTimestamp() {
		return capturedRefactoringDescriptor.getTimestamp();
	}

	public LogType getRefactoringKind() {
		return refactoringKind;
	}

	/**
	 * 
	 * @param status
	 * @return
	 */
	private int getSeverityLevel(String status) {
		if (status == null) {
			return 0;
		}
		if (status.startsWith("<OK")) {
			return 1;
		} else if (status.startsWith("<INFO")) {
			return 2;
		} else if (status.startsWith("<WARNING")) {
			return 3;
		} else if (status.startsWith("<ERROR")) {
			return 4;
		} else if (status.startsWith("<FATALERROR")) {
			return 5;
		}
		return 6;
	}

	private String getNavigationDurationString(String navigationHistoryString) {
		if (navigationHistoryString == null) {
			return "";
		} else {
			long navigationDuration;
			try {
				navigationDuration= getNavigationDuration(navigationHistoryString);
			} catch (ParseException e) {
				System.err.println(e.getMessage());
				return "";
			}
			return String.valueOf(navigationDuration);
		}
	}

	private boolean hasNavigationDuration(String navigationHistoryString) throws ParseException {
		NavigationHistory navigationHistory= NavigationHistory.parse(navigationHistoryString);
		int numberOfNavigationHistoryItems= navigationHistory.getNavigationHistoryItems().size();
		return numberOfNavigationHistoryItems >= 2;
	}

	private long getNavigationDuration(String navigationHistoryString) throws NavigationHistory.ParseException {
		if (!hasNavigationDuration(navigationHistoryString)) {
			throw new NavigationHistory.ParseException("Expected at least two items in the navigation history (" + navigationHistoryString + ") of a " + getRefactoringKind() + " "
					+ capturedRefactoringDescriptor.getID() + " refactoring, which was " + (capturedRefactoringDescriptor.isInvokedByQuickAssist() ? "" : "not ")
					+ "invoked by Quick Assist (username=" + username + ", workspace ID=" + workspaceID + ", CodingSpectator version=" + codingspectatorVersion + ", timestamp=" + getTimestamp()
					+ ").");
		}
		NavigationHistory navigationHistory= NavigationHistory.parse(navigationHistoryString);
		@SuppressWarnings("rawtypes")
		Iterator iterator= navigationHistory.getNavigationHistoryItems().iterator();
		NavigationHistoryItem currentNavigationHistoryItem= (NavigationHistoryItem)iterator.next();
		long firstTimestamp= currentNavigationHistoryItem.getTimestamp();
		while (iterator.hasNext()) {
			currentNavigationHistoryItem= (NavigationHistoryItem)iterator.next();
		}
		long lastTimestamp= currentNavigationHistoryItem.getTimestamp();
		return lastTimestamp - firstTimestamp;
	}

	private String getCodesnippetWithSelectionMarkers() {
		String codeSnippet= capturedRefactoringDescriptor.getCodeSnippet();
		String selectionInCodeSnippet= capturedRefactoringDescriptor.getSelectionInCodeSnippet();
		int selectionBeginningOffset= Integer.valueOf(selectionInCodeSnippet.split(" ")[0]);
		int selectionLength= Integer.valueOf(selectionInCodeSnippet.split(" ")[1]);
		String selectionStartMarker= "/* CODINGSPECTATOR: SELECTION BEGINS */";
		String selectionEndMarker= "/* CODINGSPECTATOR: SELECTION ENDS */";
		String codeSnippetWithSelectionMarkers= codeSnippet.substring(0, selectionBeginningOffset) + selectionStartMarker
				+ codeSnippet.subSequence(selectionBeginningOffset, selectionBeginningOffset + selectionLength) + selectionEndMarker
				+ codeSnippet.substring(selectionBeginningOffset + selectionLength);
		return codeSnippetWithSelectionMarkers;
	}

}
