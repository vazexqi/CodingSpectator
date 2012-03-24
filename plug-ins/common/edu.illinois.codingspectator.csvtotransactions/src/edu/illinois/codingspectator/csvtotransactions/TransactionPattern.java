/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.csvtotransactions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * 
 * @author Mohsen Vakilian
 * 
 */
public class TransactionPattern {

	private int identifier;

	private Set<String> items;

	static final String TRANSACTION_PATTERN_IDENTIFIER_COLUMN_NAME= "TRANSACTION_PATTERN_IDENTIFIER";

	public TransactionPattern(int identifier, Set<String> items) {
		this.identifier= identifier;
		this.items= items;
	}

	public int getIdentifier() {
		return identifier;
	}

	public void setIdentifier(int identifier) {
		this.identifier= identifier;
	}

	public Set<String> getItems() {
		return Collections.unmodifiableSet(items);
	}

	public List<String> getOrderedItems() {
		List<String> sortedItems= Arrays.asList(items.toArray(new String[] {}));
		Collections.sort(sortedItems);
		return sortedItems;
	}

}
