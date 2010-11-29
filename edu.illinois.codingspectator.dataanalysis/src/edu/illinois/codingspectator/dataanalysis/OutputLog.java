/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.dataanalysis;

import java.io.File;
import java.io.IOException;

/**
 * 
 * @author Roshanak Zilouchian
 * @author Mohsen Vakilian
 * 
 */
public class OutputLog {
	private static final String SESSIONS_END_TAG= "</sessions>";

	private static final String SESSIONS_START_TAG= "<sessions>";

	private static final String XML_VERSION_HEADER= "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

	String includeDir;

	String targetPath;

	File outputFile;

	public OutputLog(String include, String targetPath) {
		super();
		this.includeDir= include;
		this.targetPath= targetPath;

	}

	public void createLog() throws IOException {
		outputFile= FileUtilities.createFile(LogConsolidator.join(targetPath, includeDir + ".xml"));
	}

	public void addHeader() throws IOException {
		FileUtilities.append(outputFile, XML_VERSION_HEADER);
		FileUtilities.append(outputFile, SESSIONS_START_TAG);
	}

	public void addFooter() throws IOException {
		FileUtilities.append(outputFile, SESSIONS_END_TAG);
	}

	public void addFile(String path) throws IOException {
		if (path.contains(includeDir)) {
			FileUtilities.append(outputFile, new File(path));
		}
	}
}
