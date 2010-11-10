package org.eclipse.ltk.ui.refactoring.codingspectator;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class RunningModes {

	static final String DEBUG_MODE= "DEBUG_MODE"; //$NON-NLS-1$

	static final String TESTING_MODE= "TESTING_MODE"; //$NON-NLS-1$


	public static boolean isInDebugMode() {
		return System.getenv(DEBUG_MODE) != null;
	}

	public static boolean isInTestMode() {
		return System.getenv(TESTING_MODE) != null;
	}

}
