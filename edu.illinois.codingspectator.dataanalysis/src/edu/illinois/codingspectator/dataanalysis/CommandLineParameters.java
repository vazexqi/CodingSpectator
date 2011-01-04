/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.dataanalysis;

import java.util.LinkedList;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/**
 * 
 * @author Roshanak Zilouchian
 * @author Mohsen Vakilian
 * 
 */
@Parameters(commandDescription= "Traversers the input directory recursively and conslidates all log files into a few number of files.")
public class CommandLineParameters {
	@Parameter(names= { "-n", "--include" }, description= "Refactoring log files that have these names in their paths will be included in the output.")
	public List<String> includeDirs= new LinkedList<String>();

	@Parameter(names= { "-i", "--input" }, description= "The input directory that containts raw log files either directly or indirectly.")
	public String inputDir= ".";

	@Parameter(names= { "-o", "--output" }, description= "The destination directory that will contain the cosolidated log files.")
	public String outputDir= ".";

	@Parameter(names= { "-h", "--help" }, description= "Print the usage help.")
	public boolean help= false;
}
