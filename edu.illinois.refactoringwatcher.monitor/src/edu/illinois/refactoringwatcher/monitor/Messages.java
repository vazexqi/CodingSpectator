package edu.illinois.refactoringwatcher.monitor;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME= "edu.illinois.refactoringwatcher.monitor.messages"; //$NON-NLS-1$

	public static String Activator_Testing_Mode;

	public static String AuthenticationPrompter_DialogDescription;

	public static String AuthenticationPrompter_DialogDescriptionForReenteringAuthenticationInfo;

	public static String AuthenticationPrompter_DialogTitle;

	public static String AuthenticationPrompter_FailureMessage;

	public static String PrefsFacade_LastUploadTimeKey;

	public static String SecureStorage_PasswordKey;

	public static String SecureStorage_AuthenticationNodeName;

	public static String SecureStorage_UsernameKey;

	public static String Submitter_FeatureBundleName;

	public static String Submitter_LTKBundleName;

	public static String Submitter_RepositoryBaseURL;

	public static String UserValidationDialog_Password;

	public static String UserValidationDialog_SavePassword;

	public static String UserValidationDialog_Username;

	public static String WorkbenchPreferencePage_FailedToUploadMessage;

	public static String WorkbenchPreferencePage_NetidFieldPreferenceKey;

	public static String WorkbenchPreferencePage_NetidTextField;

	public static String WorkbenchPreferencePage_PluginName;

	public static String WorkbenchPreferencePage_Title;

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
