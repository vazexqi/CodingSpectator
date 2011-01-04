package edu.illinois.codingspectator.codingtracker;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	private static final String BUNDLE_NAME= "edu.illinois.codingspectator.codingtracker.messages";

	public static String Logger_CreateLogFileException;

	public static String Logger_CreateTempLogFileException;

	public static String Logger_UnrecognizedRefactoringType;

	public static String Logger_AppendLogFileException;

	public static String CodeChangeTracker_FailedToGetActiveWorkbenchWindow;

	public static String Logger_OpenKnowfilesFileException;

	public static String Logger_WriteKnownfilesFileException;

	public static String Logger_ReadUnknownFileException;

	public static String Logger_CompleteReadUnknownFileException;

	public static String CodeChangeTracker_FailedToVisitResourceDelta;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
