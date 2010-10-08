package edu.illinois.codingspectator.codingtracker;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME= "edu.illinois.codingspectator.codingtracker.messages"; //$NON-NLS-1$

	public static String Logger_CodeChangesFileName;

	public static String Logger_LTKBundleName;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
