package org.eclipse.ltk.core.refactoring.codingspectator;

import org.eclipse.ltk.core.refactoring.codingspectator.NavigationHistory.ParseException;

/**
 * @author Mohsen Vakilian
 * @author nchen
 * @author Balaji Ambresh Rajkumar
 * 
 */
public class NavigationHistoryItem {

	public static final String END_MARKER= "]"; //$NON-NLS-1$

	public static final String SEPARATOR= ","; //$NON-NLS-1$

	public static final String BEGIN_MARKER= "["; //$NON-NLS-1$

	private static final String INITIAL_DIALOG_BUTTON_LABEL= "BEGIN_REFACTORING"; //$NON-NLS-1$

	final String dialogID;

	final String buttonLabel;

	final long timestamp;

	public NavigationHistoryItem(String dialogID, String buttonLabel, long timestamp) {
		this.dialogID= dialogID;
		this.buttonLabel= buttonLabel;
		this.timestamp= timestamp;

		Logger.logDebug(toString());
	}

	/**
	 * Returns a new item that marks that this dialog was clicked through at that particular point.
	 * Because we call System.currentTimeMillis() here there might be some initial lack (albeit
	 * negligible).
	 * 
	 * @param dialogID - The title of the current dialog (whenever applicable)
	 * @param buttonLabel - The label of the button that we pressed in the corresponding dialog
	 */
	public NavigationHistoryItem(String dialogID, String buttonLabel) {
		this(dialogID, buttonLabel, System.currentTimeMillis());
	}

	public NavigationHistoryItem(String initialDialogTitle) {
		this(initialDialogTitle, INITIAL_DIALOG_BUTTON_LABEL);
	}

	public String getDialogID() {
		return dialogID;
	}

	public String getButtonLabel() {
		return buttonLabel;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public static NavigationHistoryItem parse(String navigationHistoryItemString) throws ParseException {
		if (!navigationHistoryItemString.startsWith(BEGIN_MARKER)) {
			throw new NavigationHistory.ParseException("The string representation of a navigation history item should start with " + BEGIN_MARKER);
		}
		if (!navigationHistoryItemString.endsWith(END_MARKER)) {
			throw new NavigationHistory.ParseException("The string representation of a navigation history item should end with " + END_MARKER);
		}

		navigationHistoryItemString= navigationHistoryItemString.substring(1, navigationHistoryItemString.length() - 1);
		String[] parts= navigationHistoryItemString.split(SEPARATOR);
		if (parts.length != 3) {
			throw new NavigationHistory.ParseException("Unexpected number of elements in the string representation of a navigation history item");
		}
		return new NavigationHistoryItem(parts[0], parts[1], Long.parseLong(parts[2]));
	}

	public String toString() {
		return BEGIN_MARKER + dialogID + SEPARATOR + buttonLabel + SEPARATOR + String.valueOf(timestamp) + END_MARKER;
	}

}
