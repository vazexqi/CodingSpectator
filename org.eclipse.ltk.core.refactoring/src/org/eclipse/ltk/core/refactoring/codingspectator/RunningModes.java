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

	private static final String GENERATE_EXPECTED_FILES_MODE= "GENERATE_EXPECTED"; //$NON-NLS-1$

	private static final String OVERWRITE_EXPECTED_FILES_MODE= "OVERWRITE_EXPECTED"; //$NON-NLS-1$

	public static boolean isInDebugMode() {
		return System.getenv(DEBUG_MODE) != null;
	}

	public static boolean isInTestMode() {
		return System.getenv(TEST_MODE) != null;
	}

	public static boolean shouldGenerateExpectedFiles() {
		return System.getenv(GENERATE_EXPECTED_FILES_MODE) != null;
	}

	public static boolean shouldOverwriteExpectedFiles() {
		return System.getenv(OVERWRITE_EXPECTED_FILES_MODE) != null;
	}

	public static boolean isInProductionMode() {
		return (!isInDebugMode() && !isInTestMode());
	}

}
