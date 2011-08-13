/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.logstocsv.tests;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.junit.Test;

import edu.illinois.codingspectator.logstocsv.ConvertLogsToCSV;

/**
 * 
 * @author Mohsen Vakilian
 * 
 */
public class ConvertLogsToCSVRunner {

	@Test
	public void run() throws CoreException, IOException {
		String codingspectatorDataPath= System.getenv("CS_DATA");
		String csvPath= System.getenv("CS_CSV");
		String checksAfterRefactoringsPath= System.getenv("CS_CHECKS_AFTER_REFACTORINGS");
		ConvertLogsToCSV.main(new String[] { null, codingspectatorDataPath, csvPath, checksAfterRefactoringsPath });
	}

}
