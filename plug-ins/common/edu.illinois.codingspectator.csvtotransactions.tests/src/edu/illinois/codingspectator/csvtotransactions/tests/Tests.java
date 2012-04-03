/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.csvtotransactions.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.PrintStream;

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

	private IPath expectedDetailedTransactionsFilePath;

	private IPath expectedTransactionPatternsFilePath;

	private IPath inputFilePath;

	private EFSFile actualFolder;

	private EFSFile actualFile;

	private EFSFile actualDetailedTransactionsFile;

	private EFSFile actualTransactionPatternsFile;

	private void computePaths(String testNumber) throws CoreException {
		IPath testFolderPath= new Path("resources").append(testNumber);
		String outputFileName= "transactions.txt";
		String detailedTransactionsFileName= "detailed-transactions.csv";
		String transactionPatternsFileName= "transaction-patterns.csv";
		String expectedOutputFolderName= "expected-output";
		expectedFilePath= testFolderPath.append(expectedOutputFolderName).append(outputFileName);
		expectedDetailedTransactionsFilePath= testFolderPath.append(expectedOutputFolderName).append(detailedTransactionsFileName);
		expectedTransactionPatternsFilePath= testFolderPath.append(expectedOutputFolderName).append(transactionPatternsFileName);
		IPath inputFolderPath= testFolderPath.append("input");
		inputFilePath= inputFolderPath.append("eclipse-udc-data.csv");
		actualFolder= new EFSFile(testFolderPath.append("actual-output"));
		actualFolder.mkdir();
		actualFile= actualFolder.append(outputFileName);
		actualDetailedTransactionsFile= actualFolder.append(detailedTransactionsFileName);
		actualTransactionPatternsFile= actualFolder.append(transactionPatternsFileName);
	}

	private void generateReports() throws IOException {
		System.setOut(new PrintStream(actualFile.getPath().toOSString()));
		CSVToTransactions.main(new String[] { "-n", inputFilePath.toOSString(), "-i", "description", "-s", "time", "-f", "userId", "-t", "5", "-d",
				actualDetailedTransactionsFile.getPath().toOSString(), "-p", actualTransactionPatternsFile.getPath().toOSString() });
	}

	private void checkReports() throws IOException {
		assertTrue(actualFile.exists());
		assertEquals(FileUtils.getContents(expectedFilePath.toOSString()), FileUtils.getContents(actualFile.getPath().toOSString()));
		assertTrue(actualDetailedTransactionsFile.exists());
		assertEquals(FileUtils.getContents(expectedDetailedTransactionsFilePath.toOSString()), FileUtils.getContents(actualDetailedTransactionsFile.getPath().toOSString()));
		assertTrue(actualTransactionPatternsFile.exists());
		assertEquals(FileUtils.getContents(expectedTransactionPatternsFilePath.toOSString()), FileUtils.getContents(actualTransactionPatternsFile.getPath().toOSString()));
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

	@Test
	public void test03() throws CoreException, IOException {
		testReports("03");
	}

	@Test
	public void test04() throws CoreException, IOException {
		testReports("04");
	}

	@After
	public void cleanUp() throws CoreException {
		actualFolder.delete();
	}

}
