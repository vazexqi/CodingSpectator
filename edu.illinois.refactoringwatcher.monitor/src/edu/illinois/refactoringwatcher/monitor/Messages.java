package edu.illinois.refactoringwatcher.monitor;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME= "edu.illinois.refactoringwatcher.monitor.messages"; //$NON-NLS-1$

	public static String Activator_Testing_Mode;

	public static String AuthenticationPrompter_DialogDescription;

	public static String AuthenticationPrompter_DialogTitle;

	public static String AuthenticationPrompter_FailureMessage;

	public static String AuthenticationPrompter_password;

	public static String AuthenticationPrompter_SecureStorageNodeName;

	public static String AuthenticationPrompter_username;

	public static String Submitter_ltk_bundle_name;

	public static String Submitter_repository_base_url;

	public static String WorkbenchPreferencePage_FailedToUploadMessage;

	public static String WorkbenchPreferencePage_netidFieldPreferenceKey;

	public static String WorkbenchPreferencePage_netidTextField;

	public static String WorkbenchPreferencePage_PluginName;

	public static String WorkbenchPreferencePage_title;

	public static String WorkbenchPreferencePage_UploadingMessage;

	public static String WorkbenchPreferencePage_UploadNowButtonText;

	public static String WorkbenchPreferencePage_UUIDFieldPreferenceKey;

	public static String WorkbenchPreferencePage_UUIDTextField;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
