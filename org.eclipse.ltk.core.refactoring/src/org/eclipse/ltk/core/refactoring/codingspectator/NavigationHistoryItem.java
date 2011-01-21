package org.eclipse.ltk.core.refactoring.codingspectator;

/**
 * @author Mohsen Vakilian
 * @author nchen
 */
public class NavigationHistoryItem {

	private static final String PERFORM_FINISH_BUTTON_LABEL= "OK"; //$NON-NLS-1$

	private static final String PERFORM_CANCEL_BUTTON_LABEL= "CANCEL"; //$NON-NLS-1$

	private static final String ERROR_OK_BUTTON_LABEL= "ERROR_OK"; //$NON-NLS-1$

	private static final String ERROR_BACK_BUTTON_LABEL= "ERROR_BACK"; //$NON-NLS-1$

	private static final String ERROR_CANCEL_BUTTON_LABEL= "ERROR_CANCEL"; //$NON-NLS-1$

	private static final String NEXT_OR_PREVIEW_BUTTON_LABEL= "NEXT_OR_PREVIEW"; //$NON-NLS-1$

	private static final String BACK_BUTTON_LABEL= "BACK"; //$NON-NLS-1$

	private static final String INITIAL_DIALOG_BUTTON_LABEL= "BEGIN_REFACTORING"; //$NON-NLS-1$

	private static final String FINAL_DIALOG_MARKER= "END"; //$NON-NLS-1$



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

		// DEBUG
		System.err.println(this.toString());
	}

	public NavigationHistoryItem(String initialDialogTitle) {
		this(initialDialogTitle, INITIAL_DIALOG_BUTTON_LABEL);
	}

	public static NavigationHistoryItem getPerformOKInstance() {
		return new NavigationHistoryItem(FINAL_DIALOG_MARKER, PERFORM_FINISH_BUTTON_LABEL);
	}

	public static NavigationHistoryItem getPerformCancelInstance() {
		return new NavigationHistoryItem(FINAL_DIALOG_MARKER, PERFORM_CANCEL_BUTTON_LABEL);
	}

	public static NavigationHistoryItem getOKErrorDialogInstance(String errorDialogTitle) {
		return new NavigationHistoryItem(errorDialogTitle, ERROR_OK_BUTTON_LABEL);
	}

	public static NavigationHistoryItem getCancelErrorDialogInstance(String errorDialogTitle) {
		return new NavigationHistoryItem(errorDialogTitle, ERROR_CANCEL_BUTTON_LABEL);
	}

	public static NavigationHistoryItem getBackErrorDialogInstance(String errorDialogTitle) {
		return new NavigationHistoryItem(errorDialogTitle, ERROR_BACK_BUTTON_LABEL);
	}

	public static NavigationHistoryItem getNextorPreviewPressedInstance(String errorDialogTitle) {
		return new NavigationHistoryItem(errorDialogTitle, NEXT_OR_PREVIEW_BUTTON_LABEL);
	}

	public static NavigationHistoryItem getBackPressedInstance(String errorDialogTitle) {
		return new NavigationHistoryItem(errorDialogTitle, BACK_BUTTON_LABEL);
	}

	public String toString() {
		return "[ " + dialogID + "," + buttonLabel + "," + String.valueOf(timestamp) + " ]"; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
	}

}
