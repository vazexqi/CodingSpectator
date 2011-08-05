/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.logstocsv;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.ltk.core.refactoring.codingspectator.NavigationHistory;
import org.eclipse.ltk.core.refactoring.codingspectator.NavigationHistory.ParseException;
import org.eclipse.ltk.core.refactoring.codingspectator.NavigationHistoryItem;

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
		map.put("navigation duration", getNavigationDurationString(capturedRefactoringDescriptor.getAttribute("navigation-history")));
		return map;
	}

	/**
	 * FIXME: This method may not return the right severity level for multi-level status.
	 * 
	 * @param status
	 * @return
	 */
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

	private long getNavigationDuration(String navigationHistoryString) throws NavigationHistory.ParseException {
		NavigationHistory navigationHistory= NavigationHistory.parse(navigationHistoryString);
		int numberOfNavigationHistoryItems= navigationHistory.getNavigationHistoryItems().size();
		if (numberOfNavigationHistoryItems < 2) {
			throw new NavigationHistory.ParseException("Expected at least two items in the navigation history (" + navigationHistoryString + ") of a " + refactoringKind + " refactoring.");
		}
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

}
