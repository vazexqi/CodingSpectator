/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.mergehistories;

import java.io.File;
import java.io.IOException;

import edu.illinois.codingspectator.file.utils.FileUtils;

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
		outputFile= FileUtils.createFile(LogConsolidator.join(targetPath, includeDir + ".xml"));
	}

	public void addHeader() throws IOException {
		FileUtils.append(outputFile, XML_VERSION_HEADER);
		FileUtils.append(outputFile, SESSIONS_START_TAG);
	}

	public void addFooter() throws IOException {
		FileUtils.append(outputFile, SESSIONS_END_TAG);
	}

	public void addFile(String path) throws IOException {
		if (path.contains(includeDir)) {
			FileUtilities.append(outputFile, new File(path));
		}
	}
}
