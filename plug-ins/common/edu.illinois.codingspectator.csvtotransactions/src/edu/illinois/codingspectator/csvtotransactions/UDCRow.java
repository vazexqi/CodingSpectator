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
public class UDCRow extends CSVRow {

	private static final String USER_ID_KEY= "userId";

	private static final String TIME_KEY= "time";

	private static final String DESCRIPTION_KEY= "description";

	private Map<String, String> row;

	private Transaction transaction;

	private long timeWindowInMinutes;

	public UDCRow(Map<String, String> row, long timeWindowInMinutes) {
		if (!UDCRow.isValid(row)) {
			throw new IllegalArgumentException("Invalid row:\n" + row.toString());
		}
		this.row= row;
		this.timeWindowInMinutes= timeWindowInMinutes;
	}

	private static boolean isValid(Map<String, String> row) {
		return row.containsKey(USER_ID_KEY) && row.containsKey(TIME_KEY) && row.containsKey(DESCRIPTION_KEY);
	}

	private String getUser() {
		return row.get(USER_ID_KEY);
	}

	private long getTimestamp() {
		return Long.parseLong(row.get(TIME_KEY));
	}

	@Override
	public String getItem() {
		return row.get(DESCRIPTION_KEY);
	}

	@Override
	public int hashCode() {
		return 31;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		UDCRow other= (UDCRow)obj;
		if (getItem() == null) {
			if (other.getItem() != null) {
				return false;
			}
		} else if (!getItem().equals(other.getItem())) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return getItem();
	}

	@Override
	public boolean shouldBelongToTheTransactionOf(CSVRow csvRow) {
		if (!(csvRow instanceof UDCRow)) {
			throw new IllegalArgumentException("Expected a UDCRow.");
		}
		UDCRow udcRow= (UDCRow)csvRow;
		return getUser().equals(udcRow.getUser()) && Math.abs(getTimestamp() - udcRow.getTimestamp()) <= timeWindowInMinutes * 60 * 1000;
	}

	public Transaction getTransaction() {
		return transaction;
	}

	public void setTransaction(Transaction transaction) {
		this.transaction= transaction;
		transaction.add(this);
	}

}
