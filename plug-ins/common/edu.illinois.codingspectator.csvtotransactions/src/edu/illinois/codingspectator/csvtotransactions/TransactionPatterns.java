/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.csvtotransactions;

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
		this.nextIdentifier= 0;
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
		++nextIdentifier;
		identifierToTransactionPattern.put(nextIdentifier, new TransactionPattern(nextIdentifier, items));
		return nextIdentifier;
	}

}
