/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.file.utils;

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
public class FileUtils {

	public static File createFile(String path) throws IOException {
		File file= new File(path);
		if (file.exists()) {
			file.delete();
		}
		file.getParentFile().mkdirs();
		file.createNewFile();
		return file;
	}

	public static void append(File file, String content) throws IOException {
		FileWriter fstream= new FileWriter(file, true);
		BufferedWriter out= new BufferedWriter(fstream);
		out.write(content);
		out.newLine();
		out.close();
	}

	public static String getContents(String filePath) throws IOException {
		BufferedReader fileReader= new BufferedReader(new FileReader(filePath));
		StringBuilder sb= new StringBuilder();
		String line;

		while ((line= fileReader.readLine()) != null) {
			sb.append(line);
			sb.append("\n");
		}

		fileReader.close();
		return sb.toString();
	}

}
