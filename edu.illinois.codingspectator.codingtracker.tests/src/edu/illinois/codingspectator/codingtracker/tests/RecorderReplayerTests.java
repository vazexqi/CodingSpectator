/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.tests;


/**
 * 
 * @author Stas Negara
 * 
 */
public class RecorderReplayerTests {

	public static class BasicRecorderReplayerTest extends RecorderReplayerTest {
		@Override
		protected String getTestNumber() {
			return "02";
		}

		@Override
		protected String[] getTestFileNames() {
			return new String[] { "Test1.java", "Test2.java", "Test9.java" };
		}

		@Override
		protected String[] getGeneratedFilePaths() {
			return new String[] { "/edu.illinois.testproject/src/edu/illinois/test/Test1.java",
					"/edu.illinois.testproject/src/edu/illinois/test/Test2.java",
					"/edu.illinois.test2/src/edu/illinois/test2/Test9.java" };
		}
	}

	public static class OptionsChangesRecorderReplayerTest extends RecorderReplayerTest {
		@Override
		protected String getTestNumber() {
			return "03";
		}

		@Override
		protected String[] getTestFileNames() {
			return new String[] { "Test9.java" };
		}

		@Override
		protected String[] getGeneratedFilePaths() {
			return new String[] { "/edu.illinois.test2/src/edu/illinois/test2/Test9.java" };
		}
	}

}
