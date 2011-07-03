package edu.illinois.codingspectator.branding;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME= "edu.illinois.codingspectator.branding.messages"; //$NON-NLS-1$

	public static String StatusLineBranding_status_bar_tool_tip;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
