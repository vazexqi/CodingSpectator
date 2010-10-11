/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.monitor.tests;

import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.Platform;
import org.junit.Test;
import org.osgi.framework.Version;

import edu.illinois.codingspectator.monitor.Messages;

/**
 * 
 * @author Mohsen Vakilian
 * 
 */
public class TestPluginVersion {

	@Test
	public void shouldGetPluginVersion() {
		Version ltkVersion= Platform.getBundle(Messages.Submitter_LTKBundleName).getVersion();
		assertTrue(String.format("The format of the version of %s is incorrect.", Messages.Submitter_LTKBundleName), ltkVersion.getMajor() + ltkVersion.getMinor() + ltkVersion.getMinor() > 0);
	}
}
