/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.csvtotransactions;

/**
 * 
 * @author Mohsen Vakilian
 * 
 */
public abstract class CSVRow implements Comparable<CSVRow> {

	abstract String getItem();

	abstract String getDetailedStringHeader();

	abstract String getDetailedString();

	abstract boolean shouldBelongToTheTransactionOf(CSVRow csvRow);

	@Override
	public int compareTo(CSVRow o) {
		return getItem().compareTo(o.getItem());
	}

}
