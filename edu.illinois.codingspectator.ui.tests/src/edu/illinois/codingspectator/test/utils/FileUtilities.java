/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.test.utils;

import java.io.File;

/**
 * 
 * Simple file utilities for managing the testing area between runs
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class FileUtilities {

	public static void cleanDirectory(File path) {
		if (path.isDirectory()) {
			for (File file : path.listFiles()) {
				cleanDirectory(file);
			}
		}

		path.delete();
	}
}
