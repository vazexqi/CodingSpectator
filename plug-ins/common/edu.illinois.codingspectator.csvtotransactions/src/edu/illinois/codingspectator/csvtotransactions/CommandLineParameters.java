/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.csvtotransactions;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/**
 * 
 * @author Mohsen Vakilian
 * 
 */
@Parameters(commandDescription= "Generates transactions from UDC CSV file.")
public class CommandLineParameters {

	@Parameter(names= { "-t", "--timewindow" }, description= "This number is minimum number of minutes that splits two consecutive events in two different transactions.")
	public long timeWindowInMinutes= 1;

	@Parameter(names= { "-d", "--detailedtransactions" }, description= "Path to the output CSV file that will contain detailed information about all transactions.")
	public String detailedTransactionsFileName= null;

	@Parameter(names= { "-p", "--transactionpatterns" }, description= "Path to the output CSV file that will contain the list of all transaction patterns.")
	public String transactionPatternsFileName= null;

	@Parameter(names= { "-h", "--help" }, description= "Print the usage help.")
	public boolean help= false;

}
