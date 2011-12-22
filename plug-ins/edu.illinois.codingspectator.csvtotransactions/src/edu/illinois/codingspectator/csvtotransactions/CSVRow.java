/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.csvtotransactions;

/**
 * 
 * @author Mohsen Vakilian
 * 
 */
public interface CSVRow {

	String getItem();

	boolean shouldBelongToTheTransactionOf(CSVRow csvRow);

}
