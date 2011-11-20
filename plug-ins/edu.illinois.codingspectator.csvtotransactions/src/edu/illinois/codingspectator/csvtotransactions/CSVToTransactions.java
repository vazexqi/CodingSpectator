/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.csvtotransactions;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import com.beust.jcommander.JCommander;

/**
 * 
 * @author Mohsen Vakilian
 * 
 */
public class CSVToTransactions {

	private String pathToCSVFile;

	private String pathToTransactionsFile;

	private long timeWindowInMinutes;

	public CSVToTransactions(String pathToCSVFile, String pathToTransactionsFile, long timeWindow) {
		this.pathToCSVFile= pathToCSVFile;
		this.pathToTransactionsFile= pathToTransactionsFile;
		this.timeWindowInMinutes= timeWindow;
	}

	public void convertCSVToTransactions() throws IOException {
		CSVReader csvReader= new CSVReader(pathToCSVFile);
		TransactionWriter transactionWriter= new TransactionWriter(pathToTransactionsFile);
		transactionWriter.open();
		Iterator<Map<String, String>> iterator= csvReader.iterator();
		Transaction lastTransaction= new Transaction();
		UDCRow lastRow= null;
		if (iterator.hasNext()) {
			lastRow= new UDCRow(iterator.next(), timeWindowInMinutes);
			lastRow.setTransaction(lastTransaction);
		}
		while (iterator.hasNext()) {
			UDCRow currentRow= new UDCRow(iterator.next(), timeWindowInMinutes);
			if (!currentRow.shouldBelongToTheTransactionOf(lastRow)) {
				transactionWriter.writeTransaction(lastTransaction);
				lastTransaction= new Transaction();
			}
			currentRow.setTransaction(lastTransaction);
			lastRow= currentRow;
		}
		transactionWriter.writeTransaction(lastTransaction);
		transactionWriter.close();
	}

	public static void main(String[] args) throws IOException {
		CommandLineParameters params= new CommandLineParameters();
		JCommander commander= new JCommander(params, args);
		if (params.help) {
			commander.usage();
		} else {
			CSVToTransactions csvToTransactions= new CSVToTransactions(params.inputCSVFile, params.outputTransactionsFile, params.timeWindowInMinutes);
			csvToTransactions.convertCSVToTransactions();
		}
	}

}
