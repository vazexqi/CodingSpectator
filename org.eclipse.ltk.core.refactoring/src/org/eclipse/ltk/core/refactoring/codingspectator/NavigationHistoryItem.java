package org.eclipse.ltk.core.refactoring.codingspectator;

/**
 * @author Mohsen Vakilian
 * @author nchen
 */
public class NavigationHistoryItem {

	private static final String INITIAL_DIALOG_BUTTON_LABEL= "BEGIN_REFACTORING"; //$NON-NLS-1$

	final String dialogID;

	final String buttonLabel;

	final long timestamp;

	/**
	 * Returns a new item that marks that this dialog was clicked through at that particular point.
	 * Because we call System.currentTimeMillis() here there might be some initial lack (albeit
	 * negligible).
	 * 
	 * @param dialogID - The title of the current dialog (whenever applicable)
	 * @param buttonLabel - The label of the button that we pressed in the corresponding dialog
	 */
	public NavigationHistoryItem(String dialogID, String buttonLabel) {
		this.dialogID= dialogID;
		this.buttonLabel= buttonLabel;
		this.timestamp= System.currentTimeMillis();

		Logger.logDebug(this.toString());
	}

	public NavigationHistoryItem(String initialDialogTitle) {
		this(initialDialogTitle, INITIAL_DIALOG_BUTTON_LABEL);
	}

	public String toString() {
		return "[" + dialogID + "," + buttonLabel + "," + String.valueOf(timestamp) + "]";
	}

}
