/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.monitor.ui.prefs;

import java.util.UUID;

/**
 * @author Mohsen Vakilian
 * @author nchen
 */
public class UUIDGenerator {
	public static String generateID() {
		UUID randomUUID= UUID.randomUUID();
		return randomUUID.toString();

	}
}
