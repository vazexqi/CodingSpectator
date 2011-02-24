/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.tests;

import java.io.File;

import org.junit.BeforeClass;

import edu.illinois.codingspectator.codingtracker.recording.TextRecorder;

/**
 * 
 * @author Stas Negara
 * 
 */
public abstract class TestCodingTracker {

	protected static File mainRecordFile= new File(TextRecorder.getMainRecordFilePath());

	@BeforeClass
	public static void before() {
		mainRecordFile.delete();
	}

}
