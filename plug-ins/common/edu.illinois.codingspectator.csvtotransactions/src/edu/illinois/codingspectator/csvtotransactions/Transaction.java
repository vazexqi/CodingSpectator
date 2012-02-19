/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.csvtotransactions;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * 
 * @author Mohsen Vakilian
 * 
 */
public class Transaction {

	private Set<String> items;

	public Transaction() {
		this.items= new HashSet<String>();
	}

	public void add(String item) {
		items.add(item);
	}

	@Override
	public String toString() {
		StringBuilder sb= new StringBuilder();
		Iterator<String> iterator= items.iterator();
		if (iterator.hasNext()) {
			sb.append(iterator.next());
		}
		while (iterator.hasNext()) {
			sb.append(",").append(iterator.next());
		}
		return sb.toString();
	}

}
