package edu.illinois.codingspectator.codingtracker;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME= "edu.illinois.codingspectator.codingtracker.messages";

	public static String Recorder_CreateRecordFileException;

	public static String Recorder_CreateTempRecordFileException;

	public static String Recorder_UnrecognizedRefactoringType;

	public static String Recorder_AppendRecordFileException;

	public static String CodeChangeTracker_FailedToGetActiveWorkbenchWindow;

	public static String Recorder_OpenKnowfilesFileException;

	public static String Recorder_WriteKnownfilesFileException;

	public static String Recorder_ReadUnknownFileException;

	public static String Recorder_CompleteReadUnknownFileException;

	public static String CodeChangeTracker_FailedToVisitResourceDelta;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
