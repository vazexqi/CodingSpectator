/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.csvtotransactions;

import java.util.List;
import java.util.Map;

/**
 * 
 * @author Mohsen Vakilian
 * 
 */
public class UDCRow extends CSVRow {

	private Map<String, String> row;

	private Transaction transaction;

	private String itemColumnName;

	private String timestampColumnName;

	private List<String> constantColumnNames;

	private long timeWindowInMinutes;

	public UDCRow(Map<String, String> row, String itemColumnName, String timestampColumnName, List<String> constantColumnNames, long timeWindowInMinutes) {
		if (!UDCRow.isValid(row, itemColumnName, timestampColumnName, constantColumnNames)) {
			throw new IllegalArgumentException("Invalid row:\n" + row.toString());
		}
		this.row= row;
		this.itemColumnName= itemColumnName;
		this.timestampColumnName= timestampColumnName;
		this.constantColumnNames= constantColumnNames;
		this.timeWindowInMinutes= timeWindowInMinutes;
	}

	private static boolean isValid(Map<String, String> row, String itemColumnName, String timestampColumnName, List<String> constantColumnNames) {
		return row.keySet().containsAll(constantColumnNames) && row.containsKey(timestampColumnName) && row.containsKey(itemColumnName);
	}

	public String getItemColumnName() {
		return itemColumnName;
	}

	public String getTimestampColumnName() {
		return timestampColumnName;
	}

	@Override
	public List<String> getConstantColumnNames() {
		return constantColumnNames;
	}

	private long getTimestamp() {
		return Long.parseLong(row.get(getTimestampColumnName()));
	}

	@Override
	public String getItem() {
		return row.get(getItemColumnName());
	}

	@Override
	String getDetailedStringHeader() {
		StringBuilder sb= new StringBuilder();
		for (String columnName : getConstantColumnNames()) {
			sb.append(columnName);
			sb.append(",");
		}
		sb.append(String.format("%s,%s", getTimestampColumnName(), getItemColumnName()));
		return sb.toString();
	}

	@Override
	public String getDetailedString() {
		StringBuilder sb= new StringBuilder();
		for (String columnName : getConstantColumnNames()) {
			sb.append(row.get(columnName));
			sb.append(",");
		}
		sb.append(String.format("%s,%s", row.get(getTimestampColumnName()), row.get(getItemColumnName())));
		return sb.toString();
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
		for (String columnName : getConstantColumnNames()) {
			if (!row.get(columnName).equals(udcRow.row.get(columnName))) {
				return false;
			}
		}
		return Math.abs(getTimestamp() - udcRow.getTimestamp()) <= timeWindowInMinutes * 60 * 1000;
	}

	public Transaction getTransaction() {
		return transaction;
	}

	public void setTransaction(Transaction transaction) {
		this.transaction= transaction;
		transaction.add(this);
	}

}
