/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.tests;

import java.io.File;

import org.junit.BeforeClass;

import edu.illinois.codingspectator.codingtracker.operations.JavaProjectsUpkeeper;
import edu.illinois.codingspectator.codingtracker.recording.KnownfilesRecorder;
import edu.illinois.codingspectator.codingtracker.recording.TextRecorder;

/**
 * 
 * @author Stas Negara
 * 
 */
public abstract class CodingTrackerTest {

	protected static File mainRecordFile= new File(TextRecorder.getMainRecordFilePath());

	private static File knownFilesFolder= new File(KnownfilesRecorder.getKnownFilesPath());

	@BeforeClass
	public static void before() {
		//First clear workspace, then clear the record (otherwise, the record file may get spurious operations due to closing editors),
		//and finally reset the knownfiles. 
		JavaProjectsUpkeeper.clearWorkspace();
		mainRecordFile.delete();
		resetKnownFiles();
	}

	private static void resetKnownFiles() {
		KnownfilesRecorder.getInstance().reset();
		clearFolderRecursively(knownFilesFolder);
	}

	private static void clearFolderRecursively(File folder) {
		if (folder.exists()) {
			for (File file : folder.listFiles()) {
				if (file.isDirectory()) {
					clearFolderRecursively(file);
				} else {
					file.delete();
				}
			}
			folder.delete();
		}
	}

}
