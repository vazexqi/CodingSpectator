package org.eclipse.ltk.ui.refactoring;

/**
 * 
 * @author Mohsen Vakilian
 * 
 */
public class Logger {

	private static boolean isInDebugMode() {
		return System.getenv(Messages.Logger_DebuggingMode) != null;
	}

	public static void logDebug(String debugInfo) {
		if (isInDebugMode()) {
			System.err.println(debugInfo);
		}
	}

}
