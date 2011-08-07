/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.logstocsv.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.After;
import org.junit.Test;

import edu.illinois.codingspectator.efs.EFSFile;
import edu.illinois.codingspectator.file.utils.FileUtils;
import edu.illinois.codingspectator.logstocsv.ConvertLogsToCSV;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class Tests {

	private EFSFile csvActualLogFolder;

	private IPath pathToInputFolder;

	private EFSFile csvActualLog;

	private IPath csvExpectedLog;

	private EFSFile checksAfterRefactoringsActualLog;

	private IPath checksAfterRefactoringsExpectedLog;

	private void computePaths(String testNumber) throws CoreException {
		IPath pathToTestFolder= new Path("resources").append(testNumber);
		String csvLogsFileName= "logs.csv";
		csvExpectedLog= pathToTestFolder.append("expected-output").append(csvLogsFileName);
		String checksAfterRefactoringsLogFileName= "checks-after-refactorings.csv";
		checksAfterRefactoringsExpectedLog= pathToTestFolder.append("expected-output").append(checksAfterRefactoringsLogFileName);
		pathToInputFolder= pathToTestFolder.append("input");
		csvActualLogFolder= new EFSFile(pathToTestFolder.append("actual-output"));
		csvActualLogFolder.mkdir();
		csvActualLog= csvActualLogFolder.append(csvLogsFileName);
		checksAfterRefactoringsActualLog= csvActualLogFolder.append(checksAfterRefactoringsLogFileName);
	}

	private void generateReports() throws CoreException, IOException {
		ConvertLogsToCSV.main(new String[] { null, pathToInputFolder.toOSString(), csvActualLog.getPath().toOSString(), checksAfterRefactoringsActualLog.getPath().toOSString() });
	}

	private void checkReports() throws IOException {
		assertTrue(csvActualLog.exists());
		assertEquals(FileUtils.getContents(csvExpectedLog.toOSString()), FileUtils.getContents(csvActualLog.getPath().toOSString()));
		assertTrue(checksAfterRefactoringsActualLog.exists());
		assertEquals(FileUtils.getContents(checksAfterRefactoringsExpectedLog.toOSString()), FileUtils.getContents(checksAfterRefactoringsActualLog.getPath().toOSString()));
	}

	private void testReports(String testNumber) throws CoreException, IOException {
		computePaths(testNumber);
		generateReports();
		checkReports();
	}

	@Test
	public void test01() throws CoreException, IOException {
		testReports("01");
	}

	@Test
	public void test02() throws CoreException, IOException {
		testReports("02");
	}

	@After
	public void cleanUp() throws CoreException {
		csvActualLogFolder.delete();
	}

}
