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

	public static void append(File file, String content) throws IOException {
		FileWriter fstream= new FileWriter(file, true);
		BufferedWriter out= new BufferedWriter(fstream);
		out.write(content);
		out.newLine();
		out.close();
	}

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

	public static File createFile(String path) throws IOException {
		File file= new File(path);
		if (file.exists()) {
			file.delete();
		}
		file.getParentFile().mkdirs();
		file.createNewFile();
		return file;
	}
}
