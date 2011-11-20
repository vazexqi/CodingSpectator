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

	@Parameter(names= { "-i", "--input" }, description= "Path to the input UDC CSV file.")
	public String inputCSVFile;

	@Parameter(names= { "-o", "--output" }, description= "The path to the output file containing the transactions.")
	public String outputTransactionsFile= ".";

	@Parameter(names= { "-t", "--timewindow" }, description= "This number is minimum number of minutes that splits two consecutive events in two different transactions.")
	public long timeWindowInMinutes= 1;

	@Parameter(names= { "-h", "--help" }, description= "Print the usage help.")
	public boolean help= false;

}
