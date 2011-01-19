package org.eclipse.ltk.core.refactoring.codingspectator;

/**
 * @author Mohsen Vakilian
 * @author nchen
 */
public class NavigationHistoryItem {

	final String dialogID;

	final String buttonLabel;

	final long timestamp;

	public NavigationHistoryItem(String dialogID, String buttonLabel, long timestamp) {
		this.dialogID= dialogID;
		this.buttonLabel= buttonLabel;
		this.timestamp= timestamp;
	}

	public String toString() {
		return "[ " + buttonLabel + "," + String.valueOf(timestamp) + " ]"; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
	}

}
