package org.eclipse.ltk.ui.refactoring;

/**
 * 
 * @author Mohsen Vakilian
 * 
 */
public class Logger {

	private static final String DEBUGGING_MODE= "DEBUG_MODE"; //$NON-NLS-1$

	private static boolean isInDebugMode() {
		return System.getenv(DEBUGGING_MODE) != null;
	}

	public static void logDebug(String debugInfo) {
		if (isInDebugMode()) {
			System.err.println(debugInfo);
		}
	}

}
