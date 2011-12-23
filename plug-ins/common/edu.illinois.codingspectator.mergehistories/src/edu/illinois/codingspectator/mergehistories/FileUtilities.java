/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.mergehistories;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 
 * @author Roshanak Zilouchian
 * @author Mohsen Vakilian
 * 
 */
public class FileUtilities {

	public static void append(File target, File source) throws IOException {
		FileWriter targetWriter= new FileWriter(target, true);
		FileReader sourceReader= new FileReader(source);
		BufferedWriter out= new BufferedWriter(targetWriter);
		BufferedReader in= new BufferedReader(sourceReader);

		//Skip the first line (<xml version>)
		String line= in.readLine();
		while (line != null) {
			line= in.readLine();
			if (line != null) {
				out.write(line);
				out.newLine();
			}
		}

		out.close();
		in.close();
	}

}
