/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.monitor.tests;

import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.Platform;
import org.junit.Test;
import org.osgi.framework.Version;

/**
 * 
 * @author Mohsen Vakilian
 * 
 */
public class TestPluginVersion {

	private static final String LTK_BUNDLE_NAME= "org.eclipse.ltk.core.refactoring";

	@Test
	public void shouldGetPluginVersion() {
		Version ltkVersion= Platform.getBundle(LTK_BUNDLE_NAME).getVersion();
		assertTrue(String.format("The format of the version of %s is incorrect.", LTK_BUNDLE_NAME), ltkVersion.getMajor() + ltkVersion.getMinor() + ltkVersion.getMinor() > 0);
	}
}
