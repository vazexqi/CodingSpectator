/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.tests;

import java.io.File;

import org.junit.BeforeClass;

import edu.illinois.codingspectator.codingtracker.helpers.FileHelper;
import edu.illinois.codingspectator.codingtracker.recording.TextRecorder;

/**
 * 
 * @author Stas Negara
 * 
 */
public abstract class CodingTrackerTest {

	protected static File mainRecordFile= new File(TextRecorder.getMainRecordFilePath());

	@BeforeClass
	public static void before() {
		//TODO: Consider erasing the content of knownfiles folder, because it could potentially compromise independence of tests. 
		//First clear workspace, then clear the record. Otherwise, record file may get spurious operations due to closing editors.
		FileHelper.clearWorkspace();
		mainRecordFile.delete();
	}

}
