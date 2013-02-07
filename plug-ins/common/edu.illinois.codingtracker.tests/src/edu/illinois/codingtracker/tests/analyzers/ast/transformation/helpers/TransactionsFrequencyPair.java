/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers.ast.transformation.helpers;

import java.util.Set;


/**
 * 
 * @author Stas Negara
 * 
 */
public class TransactionsFrequencyPair {

	private final Set<Integer> transactions;

	private final int frequency;


	public TransactionsFrequencyPair(Set<Integer> transactions, int frequency) {
		this.transactions= transactions;
		this.frequency= frequency;
	}

	public Set<Integer> getTransactions() {
		return transactions;
	}

	public int getFrequency() {
		return frequency;
	}

	public int getMemorySize() {
		return (frequency + transactions.size()) * 4;
	}

}
