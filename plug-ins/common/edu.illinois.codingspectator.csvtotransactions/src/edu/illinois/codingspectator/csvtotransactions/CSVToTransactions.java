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

	private OutputStreamWriter writer;

	public CSVToTransactions(InputStreamReader reader, OutputStreamWriter writer, long timeWindow) {
		this.reader= reader;
		this.writer= writer;
		this.timeWindowInMinutes= timeWindow;
	}

	public void convertCSVToTransactions() throws IOException {
		try {
			CSVReader csvReader= new CSVReader(reader, new String[] { "userId", "what", "kind", "bundleId", "bundleVersion", "description", "time" });
			TransactionWriter transactionWriter= new TransactionWriter(writer);
			Iterator<Map<String, String>> iterator= csvReader.iterator();
			Transaction lastTransaction= new Transaction();
			UDCRow lastRow= null;
			if (iterator.hasNext()) {
				lastRow= new UDCRow(iterator.next(), timeWindowInMinutes);
				lastRow.setTransaction(lastTransaction);
			}
			while (iterator.hasNext()) {
				Map<String, String> currentCSVRow= iterator.next();
				UDCRow currentRow= new UDCRow(currentCSVRow, timeWindowInMinutes);
				if (!currentRow.shouldBelongToTheTransactionOf(lastRow)) {
					transactionWriter.writeTransaction(lastTransaction);
					lastTransaction= new Transaction();
				}
				currentRow.setTransaction(lastTransaction);
				lastRow= currentRow;
			}
			transactionWriter.writeTransaction(lastTransaction);
		} finally {
			reader.close();
			writer.close();
		}
	}

	public static void main(String[] args) throws IOException {
		CommandLineParameters params= new CommandLineParameters();
		try {
			JCommander commander= new JCommander(params, args);
			if (params.help) {
				commander.usage();
			} else {
				CSVToTransactions csvToTransactions= new CSVToTransactions(new InputStreamReader(System.in), new OutputStreamWriter(System.out), params.timeWindowInMinutes);
				csvToTransactions.convertCSVToTransactions();
			}
		} catch (ParameterException e) {
			new JCommander(params).usage();
		}
	}

}
