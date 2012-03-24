/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.csvtotransactions;

/**
 * 
 * @author Mohsen Vakilian
 * 
 */
public class TransactionFactory {

	private int nextTransactionIdentifier= 1;

	public Transaction createTransaction(TransactionPatterns transactionPatterns) {
		return new Transaction(nextTransactionIdentifier++, transactionPatterns);
	}

}
