package edu.illinois.codingtracker.helpers;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME= "edu.illinois.codingtracker.helpers.messages";

	public static String Recorder_CreateRecordFileException;

	public static String Recorder_AppendRecordFileException;

	public static String CodeChangeTracker_FailedToGetActiveWorkbenchWindow;

	public static String Recorder_ReadPropertiesFromFileException;

	public static String Recorder_WritePropertiesToFileException;

	public static String Recorder_ReadFileException;

	public static String Recorder_LaunchConfigurationException;

	public static String Recorder_CompleteReadUnknownFileException;

	public static String Recorder_BadDocumentLocation;

	public static String Recorder_UnsynchronizedDocumentNotifications;

	public static String Recorder_CVSEntriesCopyFailure;

	public static String Recorder_CVSFolderMembersFailure;

	public static String CodeChangeTracker_FailedToVisitResourceDelta;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
