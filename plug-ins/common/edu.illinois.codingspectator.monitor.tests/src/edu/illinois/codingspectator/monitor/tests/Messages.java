package edu.illinois.codingspectator.monitor.tests;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME= "edu.illinois.codingspectator.monitor.tests.messages"; //$NON-NLS-1$

	public static String MockAuthenticationProvider_TestRepositoryURL;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
