/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.csvtotransactions;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author Mohsen Vakilian
 * 
 */
public class TransactionPatterns {

	private int nextIdentifier;

	private Map<Integer, TransactionPattern> identifierToTransactionPattern;

	public TransactionPatterns() {
		this.nextIdentifier= 1;
		this.identifierToTransactionPattern= new HashMap<Integer, TransactionPattern>();
	}

	public int findTransactionPattern(Set<String> items) {
		for (TransactionPattern transactionPattern : identifierToTransactionPattern.values()) {
			if (transactionPattern.getItems().equals(items)) {
				return transactionPattern.getIdentifier();
			}
		}
		return -1;
	}

	public int addTransactionPattern(Set<String> items) {
		int identifier= nextIdentifier;
		identifierToTransactionPattern.put(identifier, new TransactionPattern(identifier, items));
		++nextIdentifier;
		return identifier;
	}

	public void writeTo(Writer transactionPatternsWriter) {
		try {
			transactionPatternsWriter.write("TRANSACTION_PATTERN_IDENTIFIER,ITEM\n");
			for (int i= 1; i <= identifierToTransactionPattern.size(); ++i) {
				TransactionPattern transactionPattern= identifierToTransactionPattern.get(i);
				for (String item : transactionPattern.getOrderedItems()) {
					transactionPatternsWriter.write(String.format("%s,%s\n", transactionPattern.getIdentifier(), item));
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}
}
