/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.csvtotransactions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * 
 * @author Mohsen Vakilian
 * 
 */
public class Transaction {

	private List<CSVRow> rows;

	private TransactionPatterns transactionPatterns;

	private static int nextTransactionIdentifier= 0;
	
	private int transactionIdentifier;

	private int transactionPatternIdentifier;

	private Transaction(int transactionIdentifier, TransactionPatterns transactionPatterns) {
		this.transactionIdentifier= transactionIdentifier;
		this.rows= new ArrayList<CSVRow>();
		this.transactionPatterns= transactionPatterns;
	}

	public static Transaction createTransaction(TransactionPatterns transactionPatterns) {
		++nextTransactionIdentifier;
		return new Transaction(nextTransactionIdentifier, transactionPatterns);
	}

	public void add(CSVRow item) {
		rows.add(item);
	}

	private List<CSVRow> getNormalizedRows() {
		Set<CSVRow> itemsSet= new HashSet<CSVRow>();
		itemsSet.addAll(rows);
		List<CSVRow> sortedItems= Arrays.asList(itemsSet.toArray(new CSVRow[] {}));
		Collections.sort(sortedItems);
		return sortedItems;
	}

	private Set<String> getItemsSet() {
		Set<String> itemsSet= new HashSet<String>();
		for (CSVRow row : rows) {
			itemsSet.add(row.getItem());
		}
		return itemsSet;
	}

	private void setTransactionPatternIdentifier() {
		Set<String> itemsSet= getItemsSet();
		transactionPatternIdentifier= transactionPatterns.findTransactionPattern(itemsSet);
		if (transactionPatternIdentifier == -1) {
			transactionPatternIdentifier= transactionPatterns.addTransactionPattern(itemsSet);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb= new StringBuilder();
		Iterator<CSVRow> iterator= getNormalizedRows().iterator();
		if (iterator.hasNext()) {
			sb.append(iterator.next().toString());
		}
		while (iterator.hasNext()) {
			sb.append(",").append(iterator.next());
		}
		return sb.toString();
	}

}
