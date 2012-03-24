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

	private int transactionIdentifier;

	private int transactionPatternIdentifier;

	public Transaction(int transactionIdentifier, TransactionPatterns transactionPatterns) {
		this.transactionIdentifier= transactionIdentifier;
		this.rows= new ArrayList<CSVRow>();
		this.transactionPatterns= transactionPatterns;
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

	public void setTransactionPatternIdentifier() {
		Set<String> itemsSet= getItemsSet();
		transactionPatternIdentifier= transactionPatterns.findTransactionPattern(itemsSet);
		if (transactionPatternIdentifier == -1) {
			transactionPatternIdentifier= transactionPatterns.addTransactionPattern(itemsSet);
		}
	}

	public String getDetailedStringHeader() {
		String header= String.format("%s,%s", "TRANSACTION_IDENTIFIER", "TRANSACTION_PATTERN_IDENTIFIER");
		if (!rows.isEmpty()) {
			header+= "," + rows.get(0).getDetailedStringHeader();
		}
		return header;
	}

	public String getDetailedString() {
		StringBuilder sb= new StringBuilder();
		Iterator<CSVRow> iterator= rows.iterator();
		boolean isFirstRow= true;
		while (iterator.hasNext()) {
			String nextRowString= String.format("%s,%s,%s", transactionIdentifier, transactionPatternIdentifier, iterator.next().getDetailedString());
			if (!isFirstRow) {
				sb.append("\n");
			}
			sb.append(nextRowString);
			isFirstRow= false;
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		StringBuilder sb= new StringBuilder();
		Iterator<CSVRow> iterator= getNormalizedRows().iterator();
		boolean isFirstRow= true;
		while (iterator.hasNext()) {
			if (!isFirstRow) {
				sb.append(",");
			}
			sb.append(iterator.next().toString());
			isFirstRow= false;
		}
		return sb.toString();
	}

}
