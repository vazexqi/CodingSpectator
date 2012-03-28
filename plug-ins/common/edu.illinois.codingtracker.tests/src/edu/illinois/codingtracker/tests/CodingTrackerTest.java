/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests;

import java.io.File;

import org.junit.BeforeClass;

import edu.illinois.codingtracker.operations.JavaProjectsUpkeeper;
import edu.illinois.codingtracker.recording.ASTInferenceTextRecorder;
import edu.illinois.codingtracker.recording.KnownFilesRecorder;
import edu.illinois.codingtracker.recording.TextRecorder;
import edu.illinois.codingtracker.recording.ast.ASTOperationRecorder;
import edu.illinois.codingtracker.recording.ast.identification.ASTNodesIdentifier;

/**
 * 
 * @author Stas Negara
 * 
 */
public abstract class CodingTrackerTest {

	protected static File mainRecordFile= new File(TextRecorder.getMainRecordFilePath());

	protected static File astMainRecordFile= new File(ASTInferenceTextRecorder.getMainRecordFilePath());

	private static File knownFilesFolder= new File(KnownFilesRecorder.getKnownFilesPath());

	@BeforeClass
	public static void before() {
		//First clear workspace, then clear the record (otherwise, the record file may get spurious operations due to closing editors),
		//and finally reset the known files. 
		JavaProjectsUpkeeper.clearWorkspace();
		mainRecordFile.delete();
		astMainRecordFile.delete();
		resetKnownFiles();
		ASTNodesIdentifier.resetIDs();
		ASTOperationRecorder.getInstance().resetState();
	}

	private static void resetKnownFiles() {
		KnownFilesRecorder.getInstance().reset();
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
