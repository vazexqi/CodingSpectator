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
public class TransactionWriter {

	private Writer writer;

	public TransactionWriter(Writer writer) {
		this.writer= writer;
	}

	public void writeTransaction(Transaction transaction) throws IOException {
		writer.write(transaction.toString() + "\n");
	}

}
