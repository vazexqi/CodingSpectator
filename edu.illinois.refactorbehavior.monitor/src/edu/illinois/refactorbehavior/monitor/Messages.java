package edu.illinois.refactorbehavior.monitor;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME= "edu.illinois.refactorbehavior.monitor.messages"; //$NON-NLS-1$

	public static String WorkbenchPreferencePage_netidFieldPreferenceKey;

	public static String WorkbenchPreferencePage_netidTextField;

	public static String WorkbenchPreferencePage_title;

	public static String WorkbenchPreferencePage_UUIDFieldPreferenceKey;

	public static String WorkbenchPreferencePage_UUIDTextField;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
