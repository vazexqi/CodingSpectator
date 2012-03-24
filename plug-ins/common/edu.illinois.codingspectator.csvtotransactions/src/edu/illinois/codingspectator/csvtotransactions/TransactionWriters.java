/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.csvtotransactions;

import java.io.IOException;
import java.io.Writer;

/**
 * 
 * @author Mohsen Vakilian
 * 
 */
public class TransactionWriters {

	private Writer transactionWriter;

	private boolean hasWrittenDetailedTransactionsHeader;

	private Writer detailedTransactionsWriter;

	public TransactionWriters(Writer transactionWriter, Writer detailedTransactionsWriter) {
		this.hasWrittenDetailedTransactionsHeader= false;
		this.transactionWriter= transactionWriter;
		this.detailedTransactionsWriter= detailedTransactionsWriter;
	}

	public void writeTransaction(Transaction transaction) throws IOException {
		transaction.setTransactionPatternIdentifier();
		transactionWriter.write(transaction.toString() + "\n");
		if (detailedTransactionsWriter != null) {
			if (!hasWrittenDetailedTransactionsHeader) {
				detailedTransactionsWriter.write(transaction.getDetailedStringHeader() + "\n");
				hasWrittenDetailedTransactionsHeader= true;
			}
			detailedTransactionsWriter.write(transaction.getDetailedString() + "\n");
		}
	}

	public void close() {
		try {
			transactionWriter.close();
			if (detailedTransactionsWriter != null) {
				detailedTransactionsWriter.close();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
