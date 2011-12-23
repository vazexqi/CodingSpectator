/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.csvtotransactions;

import java.util.Map;

/**
 * 
 * @author Mohsen Vakilian
 * 
 */
public class UDCRow implements CSVRow {

	private Map<String, String> row;

	private Transaction transaction;

	private long timeWindowInMinutes;

	public UDCRow(Map<String, String> row, long timeWindowInMinutes) {
		this.row= row;
		this.timeWindowInMinutes= timeWindowInMinutes;
	}

	private String getUser() {
		return row.get("userId");
	}

	private long getTimestamp() {
		return Long.parseLong(row.get("time"));
	}

	@Override
	public String getItem() {
		return row.get("description");
	}

	@Override
	public boolean shouldBelongToTheTransactionOf(CSVRow csvRow) {
		if (!(csvRow instanceof UDCRow)) {
			return false;
		}
		UDCRow udcRow= (UDCRow)csvRow;
		return getUser().equals(udcRow.getUser()) && Math.abs(getTimestamp() - udcRow.getTimestamp()) <= timeWindowInMinutes * 60 * 1000;
	}

	public Transaction getTransaction() {
		return transaction;
	}

	public void setTransaction(Transaction transaction) {
		this.transaction= transaction;
		transaction.add(getItem());
	}

}
