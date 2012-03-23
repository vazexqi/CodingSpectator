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

	private Writer detailedTransactionsWriter;

	private Writer transactionPatternsWriter;

	public TransactionWriters(Writer transactionWriter, Writer detailedTransactionsWriter, Writer transactionPatternsWriter) {
		this.transactionWriter= transactionWriter;
		this.detailedTransactionsWriter= detailedTransactionsWriter;
		this.transactionPatternsWriter= transactionPatternsWriter;
	}

	public void writeTransaction(Transaction transaction) throws IOException {
		transactionWriter.write(transaction.toString() + "\n");
	}

	public void close() {
		try {
			transactionWriter.close();
			detailedTransactionsWriter.close();
			transactionPatternsWriter.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
