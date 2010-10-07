package edu.illinois.codingspectator.monitor.prefs;

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
