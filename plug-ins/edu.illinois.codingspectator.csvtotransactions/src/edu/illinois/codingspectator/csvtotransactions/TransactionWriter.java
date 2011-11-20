/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.csvtotransactions;

import java.io.FileWriter;
import java.io.IOException;


/**
 * 
 * @author Mohsen Vakilian
 * 
 */
public class TransactionWriter {

	enum Status {
		OPEN, CLOSED
	};

	private String pathToTransactionsFile;

	private Status status;

	private FileWriter fileWriter;

	public TransactionWriter(String pathToTransactionsFile) {
		this.pathToTransactionsFile= pathToTransactionsFile;
		this.status= Status.CLOSED;
	}

	public void open() throws IOException {
		fileWriter= new FileWriter(pathToTransactionsFile);
		this.status= Status.OPEN;
	}

	public void writeTransaction(Transaction transaction) throws IOException {
		if (status != Status.OPEN) {
			throw new RuntimeException("Transaction file is not open.");
		}
		fileWriter.write(transaction.toString() + "\n");
	}

	public void close() throws IOException {
		if (status == Status.OPEN) {
			fileWriter.close();
			status= Status.CLOSED;
		}
	}

}
