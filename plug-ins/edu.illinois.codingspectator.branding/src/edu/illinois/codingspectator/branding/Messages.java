/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.branding;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author Mohsen Vakilian
 * 
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME= "edu.illinois.codingspectator.branding.messages"; //$NON-NLS-1$

	public static String StatusLineBranding_status_bar_tool_tip;

	public static String WorkbenchPreferencePage_preference_page_description;

	public static String WorkbenchPreferencePage_show_bundle_in_status_line;

	public static String WorkbenchPreferencePage_show_bundle_in_status_line_tool_tip;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
