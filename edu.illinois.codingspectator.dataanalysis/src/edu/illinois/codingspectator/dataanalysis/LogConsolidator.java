/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.dataanalysis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.beust.jcommander.JCommander;

/**
 * 
 * @author Roshanak Zilouchian
 * @author Mohsen Vakilian
 * 
 */
public class LogConsolidator {

	private static final String REFACTORING_FILENAME= "refactorings.history";

	private List<OutputLog> outputLogs= new ArrayList<OutputLog>();

	public static String join(String parent, String child) {
		return parent + File.separator + child;
	}

	public void combine(String codingSpectatorDataPath, String targetPath, Set<String> includeDirs) throws IOException {
		for (String includeDir : includeDirs) {
			OutputLog outputLog= new OutputLog(includeDir, targetPath);
			outputLog.createLog();
			outputLog.addHeader();
			outputLogs.add(outputLog);
		}

		combineFiles(codingSpectatorDataPath);

		for (OutputLog outputLog : outputLogs) {
			outputLog.addFooter();
		}

	}

	public void combineFiles(String path) throws IOException {
		File rootDir= new File(path);
		if (rootDir.getName().equals(REFACTORING_FILENAME)) {
			addFile(path);
		} else {
			if (rootDir.isDirectory()) {
				String[] childrenDirs= rootDir.list();
				for (String childDir : childrenDirs) {
					combineFiles(join(rootDir.getPath(), childDir));
				}
			}
		}

	}

	private void addFile(String path) throws IOException {
		for (OutputLog outputLog : outputLogs) {
			outputLog.addFile(path);
		}
	}


	public static void main(String[] args) throws IOException {
		CommandLineParameters params= new CommandLineParameters();
		JCommander commander= new JCommander(params, args);
		if (params.help) {
			commander.usage();
		} else {
			LogConsolidator logConsolidator= new LogConsolidator();
			logConsolidator.combine(params.inputDir, params.outputDir, new HashSet<String>(params.includeDirs));
		}
	}
}
