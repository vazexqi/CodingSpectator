package edu.illinois.eclipsewatcher.test.utils;

import java.io.File;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 *         Simple file utilities for managing the testing area between runs
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
