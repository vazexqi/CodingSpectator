/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.csvtotransactions.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.junit.After;
import org.junit.Test;

import edu.illinois.codingspectator.csvtotransactions.CSVToTransactions;
import edu.illinois.codingspectator.efs.EFSFile;
import edu.illinois.codingspectator.file.utils.FileUtils;

/**
 * 
 * @author Mohsen Vakilian
 * 
 */
public class Tests {

	private IPath expectedFilePath;

	private IPath inputFilePath;

	private EFSFile actualFolder;

	private EFSFile actualFile;

	private void computePaths(String testNumber) throws CoreException {
		IPath testFolderPath= new Path("resources").append(testNumber);
		String outputFileName= "transactions.txt";
		expectedFilePath= testFolderPath.append("expected-output").append(outputFileName);
		IPath inputFolderPath= testFolderPath.append("input");
		inputFilePath= inputFolderPath.append("eclipse-udc-data.csv");
		actualFolder= new EFSFile(testFolderPath.append("actual-output"));
		actualFolder.mkdir();
		actualFile= actualFolder.append(outputFileName);
	}

	private void generateReports() throws IOException {
		CSVToTransactions.main(new String[] { "-i", inputFilePath.toOSString(), "-o", actualFile.getPath().toOSString(), "-t", "5" });
	}

	private void checkReports() throws IOException {
		assertTrue(actualFile.exists());
		assertEquals(FileUtils.getContents(expectedFilePath.toOSString()), FileUtils.getContents(actualFile.getPath().toOSString()));
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

	@After
	public void cleanUp() throws CoreException {
		actualFolder.delete();
	}

}
