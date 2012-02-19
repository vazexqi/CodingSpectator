package edu.illinois.codingspectator.monitor.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME= "edu.illinois.codingspectator.monitor.ui.messages"; //$NON-NLS-1$

	public static String AuthenticationPrompter_DialogDescription;

	public static String AuthenticationPrompter_DialogDescriptionForReenteringAuthenticationInfo;

	public static String AuthenticationPrompter_DialogTitle;

	public static String AuthenticationPrompter_FailureMessage;

	public static String MainPreferencePage_PreferencePageDescription;

	public static String PrefsFacade_ForcedAutomaticUpdateHasBeenSetKey;

	public static String PrefsFacade_LastUploadTimeKey;

	public static String SecureStorage_PasswordKey;

	public static String SecureStorage_AuthenticationNodeName;

	public static String SecureStorage_UsernameKey;

	public static String UserValidationDialog_Password;

	public static String UserValidationDialog_SavePassword;

	public static String UserValidationDialog_Username;

	public static String UploadingPreferencePage_LastUploadTextField;

	public static String UploadingPreferencePage_Description;

	public static String UploadingPreferencePage_UploadNowButtonText;

	public static String UploadingPreferencePage_UUIDFieldPreferenceKey;

	public static String UploadingPreferencePage_UUIDTextField;

	public static String PluginName;

	public static String Uploader_FailedToUploadMessage;

	public static String Uploader_UploadingMessage;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
