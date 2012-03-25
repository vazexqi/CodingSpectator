/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.csvtotransactions;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/**
 * 
 * @author Mohsen Vakilian
 * 
 */
@Parameters(commandDescription= "Generates transactions from a CSV file of items.")
public class CommandLineParameters {

	@Parameter(names= { "-i", "--itemcolumn" }, description= "Name of the column of the CVS file that contains the items.", required= true)
	public String itemColumnName;

	@Parameter(names= { "-s", "--timestampcolumn" }, description= "Name of the column of the CVS file that contains the timestamps of items.")
	public String timestampColumnName= "TIMESTAMP";

	@Parameter(names= { "-f", "--fixedcolumns" }, description= "Name of the columns names that should be fixed within a transaction.")
	public List<String> fixedColumnNames= new ArrayList<String>();

	@Parameter(names= { "-t", "--timewindow" }, description= "This number is minimum number of minutes that splits two consecutive events in two different transactions.")
	public long timeWindowInMinutes= 1;

	@Parameter(names= { "-d", "--detailedtransactions" }, description= "Path to the output CSV file that will contain detailed information about all transactions.")
	public String detailedTransactionsFileName= null;

	@Parameter(names= { "-p", "--transactionpatterns" }, description= "Path to the output CSV file that will contain the list of all transaction patterns.")
	public String transactionPatternsFileName= null;

	@Parameter(names= { "-h", "--help" }, description= "Print the usage help.")
	public boolean help= false;

}
