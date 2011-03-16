package org.eclipse.ltk.core.refactoring.codingspectator;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class RunningModes {

	private static final String DEBUG_MODE= "DEBUG_MODE"; //$NON-NLS-1$

	private static final String TEST_MODE= "TESTING_MODE"; //$NON-NLS-1$

	public static boolean isInDebugMode() {
		return System.getenv(DEBUG_MODE) != null;
	}

	public static boolean isInTestMode() {
		return System.getenv(TEST_MODE) != null;
	}

	public static boolean isInProductionMode() {
		return (!isInDebugMode() && !isInTestMode());
	}

}
