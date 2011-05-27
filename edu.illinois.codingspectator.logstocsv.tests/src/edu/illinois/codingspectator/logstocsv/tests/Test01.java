/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.logstocsv.tests;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.Test;

import edu.illinois.codingspectator.efs.EFSFile;
import edu.illinois.codingspectator.logstocsv.ConvertLogsToCSV;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class Test01 {

	@Test
	public void test() throws CoreException, IOException {
		IPath pathToTestFolder= new Path("resources").append("01");
		String pathToUserFolder= pathToTestFolder.append("input").append("username").toOSString();
		EFSFile csvLogFile= new EFSFile(pathToTestFolder.append("expected-output").append("logs.csv"));
		ConvertLogsToCSV.main(new String[] { null, pathToUserFolder, csvLogFile.getPath().toOSString() });
		assertTrue(csvLogFile.exists());
	}

}
