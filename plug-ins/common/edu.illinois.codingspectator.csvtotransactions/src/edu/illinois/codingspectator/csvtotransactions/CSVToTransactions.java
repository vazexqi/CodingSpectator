/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.csvtotransactions;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.Map;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

/**
 * 
 * @author Mohsen Vakilian
 * 
 */
public class CSVToTransactions {

	private long timeWindowInMinutes;

	private InputStreamReader reader;

	private TransactionWriters transactionWriters;

	OutputStreamWriter detailedTransactionsWriter;

	OutputStreamWriter transactionPatternsWriter;

	public CSVToTransactions(InputStreamReader reader, OutputStreamWriter transactionWriter, long timeWindow, OutputStreamWriter detailedTransactionsWriter,
			OutputStreamWriter transactionPatternsWriter) {
		this.reader= reader;
		this.transactionWriters= new TransactionWriters(transactionWriter, detailedTransactionsWriter, transactionPatternsWriter);
	}

	public void convertCSVToTransactions() throws IOException {
		try {
			TransactionPatterns transactionPatterns= new TransactionPatterns();
			CSVReader csvReader= new CSVReader(reader, new String[] { "userId", "description", "time" });
			Iterator<Map<String, String>> iterator= csvReader.iterator();
			Transaction lastTransaction= Transaction.createTransaction(transactionPatterns);
			UDCRow lastRow= null;
			if (iterator.hasNext()) {
				lastRow= new UDCRow(iterator.next(), timeWindowInMinutes);
				lastRow.setTransaction(lastTransaction);
			}
			while (iterator.hasNext()) {
				Map<String, String> currentCSVRow= iterator.next();
				UDCRow currentRow= new UDCRow(currentCSVRow, timeWindowInMinutes);
				if (!currentRow.shouldBelongToTheTransactionOf(lastRow)) {
					transactionWriters.writeTransaction(lastTransaction);
					lastTransaction= Transaction.createTransaction(transactionPatterns);
				}
				currentRow.setTransaction(lastTransaction);
				lastRow= currentRow;
			}
			transactionWriters.writeTransaction(lastTransaction);
		} finally {
			reader.close();
			transactionWriters.close();
		}
	}

	public static void main(String[] args) throws IOException {
		CommandLineParameters params= new CommandLineParameters();
		try {
			JCommander commander= new JCommander(params, args);
			if (params.help) {
				commander.usage();
			} else {
				CSVToTransactions csvToTransactions= new CSVToTransactions(new InputStreamReader(System.in), new OutputStreamWriter(System.out), params.timeWindowInMinutes, null, null);
				csvToTransactions.convertCSVToTransactions();
			}
		} catch (ParameterException e) {
			new JCommander(params).usage();
		}
	}

}
