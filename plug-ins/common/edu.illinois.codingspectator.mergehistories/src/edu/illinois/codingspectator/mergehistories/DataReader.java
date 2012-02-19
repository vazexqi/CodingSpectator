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
public class DataReader {

	private static final String SESSIONS_END_TAG= "</sessions>";

	private static final String REFACTORING_FILENAME= "refactorings.history";

	private static final String SESSIONS_START_TAG= "<sessions>";

	private static final String XML_VERSION_HEADER= "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

	private File canceledMasterFile;

	private File performedMasterFile;

	public static String join(String parent, String child) {
		return parent + File.separator + child;
	}

	public void combine(String codingSpectatorDataPath, String targetPath) throws IOException {
		canceledMasterFile= createFile(join(targetPath, "canceled.xml"));
		performedMasterFile= createFile(join(targetPath, "performed.xml"));

		append(canceledMasterFile, XML_VERSION_HEADER);
		append(canceledMasterFile, SESSIONS_START_TAG);

		append(performedMasterFile, XML_VERSION_HEADER);
		append(performedMasterFile, SESSIONS_START_TAG);

		combineFiles(codingSpectatorDataPath, REFACTORING_FILENAME);

		append(canceledMasterFile, SESSIONS_END_TAG);
		append(performedMasterFile, SESSIONS_END_TAG);

	}

	private File createFile(String path) {
		File file= new File(path);
		if (file.exists())
			file.delete();
		return file;
	}

	public void combineFiles(String path, String type) throws IOException {
		File mainDir= new File(path);
		if (mainDir.isDirectory()) {
			String[] children= mainDir.list();
			for (String dir : children) {
				if (dir.equals(type)) {
					addFile(join(mainDir.getPath(), type));
				} else {
					combineFiles(join(mainDir.getPath(), dir), type);
				}
			}
		}

	}

	private void addFile(String path) throws IOException {
		if (path.contains("canceled"))
			append(canceledMasterFile, new File(path));
		else if (path.contains("performed"))
			append(performedMasterFile, new File(path));
	}

	private void append(File file, String content) throws IOException {
		FileWriter fstream= new FileWriter(file, true);
		BufferedWriter out= new BufferedWriter(fstream);
		out.write(content);
		out.newLine();
		out.close();
	}

	private void append(File target, File source) throws IOException {
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

	public static void main(String[] args) throws IOException {
		DataReader dataReader= new DataReader();
		dataReader.combine(args[0], args[1]);
	}
}
