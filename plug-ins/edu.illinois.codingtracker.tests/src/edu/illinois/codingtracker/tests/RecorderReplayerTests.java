/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests;


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

	public static class ReferencingProjectsChangesRecorderReplayerTest extends RecorderReplayerTest {
		@Override
		protected String getTestNumber() {
			return "04";
		}

		@Override
		protected String[] getTestFileNames() {
			return new String[] { "BaseClassRenamed5.java", "DerivedClass.java", "OtherDerivedClass.java" };
		}

		@Override
		protected String[] getGeneratedFilePaths() {
			return new String[] { "/edu.illinois.test/src2/edu/illinois/testt/BaseClassRenamed5.java",
					"/edu.illinois.test/src2/edu/illinois/testt/DerivedClass.java",
					"/edu.illinois.test2/src/edu/illinois/test2/OtherDerivedClass.java" };
		}
	}

	public static class RefreshEditorsRecorderReplayerTest extends RecorderReplayerTest {
		@Override
		protected String getTestNumber() {
			return "05";
		}

		@Override
		protected String[] getTestFileNames() {
			return new String[] { "BaseClassRenamed5.java", "DerivedClass.java" };
		}

		@Override
		protected String[] getGeneratedFilePaths() {
			return new String[] { "/edu.illinois.test/src2/edu/illinois/testt/BaseClassRenamed5.java",
					"/edu.illinois.test/src2/edu/illinois/testt/DerivedClass.java" };
		}
	}

	public static class ConflictEditorsRecorderReplayerTest extends RecorderReplayerTest {
		@Override
		protected String getTestNumber() {
			return "06";
		}

		@Override
		protected String[] getTestFileNames() {
			return new String[] { "Test1.java", "Test2.java" };
		}

		@Override
		protected String[] getGeneratedFilePaths() {
			return new String[] { "/edu.illinois.test2/src/edu/illinois/test2/Test1.java",
					"/edu.illinois.test2/src/edu/illinois/test2/Test2.java" };
		}
	}

	public static class ExtractMethodRefactoringRecorderReplayerTest extends RecorderReplayerTest {
		@Override
		protected String getTestNumber() {
			return "11";
		}

		@Override
		protected String[] getTestFileNames() {
			return new String[] { "BaseClass.java" };
		}

		@Override
		protected String[] getGeneratedFilePaths() {
			return new String[] { "/edu.illinois.test/src/edu/illinois/test/BaseClass.java" };
		}
	}

	/**
	 * The first group includes refactorings: Rename, Move, Copy, and Delete at different levels -
	 * from CompilationUnit level to higher.
	 * 
	 */
	public static class RenameMoveCopyDeleteRefactoringRecorderReplayerTest extends RecorderReplayerTest {
		@Override
		protected String getTestNumber() {
			return "12";
		}

		@Override
		protected String[] getTestFileNames() {
			return new String[] { "MyActivator2.java" };
		}

		@Override
		protected String[] getGeneratedFilePaths() {
			return new String[] { "/edu.illinois.test2/MyActivator2.java" };
		}
	}

	/**
	 * The second group includes refactorings: Change Method Signature, Extract Method, Extract
	 * Local Variable, Extract Constant, and Inline. Also, here are a couple of Rename refactorings
	 * at the class or lower level.
	 * 
	 */
	public static class EclipseRefactoringsGroup2RecorderReplayerTest extends RecorderReplayerTest {
		@Override
		protected String getTestNumber() {
			return "13";
		}

		@Override
		protected String[] getTestFileNames() {
			return new String[] { "BaseTest2.java", "DerivedTest1.java", "DerivedTest2.java" };
		}

		@Override
		protected String[] getGeneratedFilePaths() {
			return new String[] { "/edu.illinois.test/src/p1/p2/BaseTest2.java", "/edu.illinois.test/src/p1/p2/DerivedTest1.java", "/edu.illinois.test/src/p1/p2/DerivedTest2.java" };
		}
	}

	/**
	 * The third group includes refactorings: Convert Local Variable to Field, Convert Anonymous
	 * Class to Nested, and Move Type to New File.
	 * 
	 */
	public static class EclipseRefactoringsGroup3RecorderReplayerTest extends RecorderReplayerTest {
		@Override
		protected String getTestNumber() {
			return "14";
		}

		@Override
		protected String[] getTestFileNames() {
			return new String[] { "BaseTest2.java", "DerivedTest1.java", "DerivedTest2.java", "MyRunnable.java" };
		}

		@Override
		protected String[] getGeneratedFilePaths() {
			return new String[] { "/edu.illinois.test/src/p1/p2/BaseTest2.java", "/edu.illinois.test/src/p1/p2/DerivedTest1.java", "/edu.illinois.test/src/p1/p2/DerivedTest2.java",
					"/edu.illinois.test/src/p1/p2/MyRunnable.java" };
		}
	}

	/**
	 * The fourth group includes refactorings: Extract Superclass, Extract Interface, Use Supertype
	 * Where Possible, Push Down, and Pull Up.
	 * 
	 */
	public static class EclipseRefactoringsGroup4RecorderReplayerTest extends RecorderReplayerTest {
		@Override
		protected String getTestNumber() {
			return "15";
		}

		@Override
		protected String[] getTestFileNames() {
			return new String[] { "BaseTest2.java", "DerivedTest1.java", "DerivedTest2.java", "MyRunnable.java", "BaseBaseTest.java", "BaseInterface.java" };
		}

		@Override
		protected String[] getGeneratedFilePaths() {
			return new String[] { "/edu.illinois.test/src/p1/p2/BaseTest2.java", "/edu.illinois.test/src/p1/p2/DerivedTest1.java", "/edu.illinois.test/src/p1/p2/DerivedTest2.java",
					"/edu.illinois.test/src/p1/p2/MyRunnable.java", "/edu.illinois.test/src/p1/p2/BaseBaseTest.java", "/edu.illinois.test/src/p1/p2/BaseInterface.java" };
		}
	}

	/**
	 * The fifth group includes refactorings: Extract Class and Introduce Parameter Object.
	 * 
	 */
	public static class EclipseRefactoringsGroup5RecorderReplayerTest extends RecorderReplayerTest {
		@Override
		protected String getTestNumber() {
			return "16";
		}

		@Override
		protected String[] getTestFileNames() {
			return new String[] { "BaseTest2.java", "DerivedTest1.java", "DerivedTest2.java", "MyRunnable.java", "BaseBaseTest.java", "BaseInterface.java", "BaseBaseTestData.java",
					"MultipleParameters.java" };
		}

		@Override
		protected String[] getGeneratedFilePaths() {
			return new String[] { "/edu.illinois.test/src/p1/p2/BaseTest2.java", "/edu.illinois.test/src/p1/p2/DerivedTest1.java", "/edu.illinois.test/src/p1/p2/DerivedTest2.java",
					"/edu.illinois.test/src/p1/p2/MyRunnable.java", "/edu.illinois.test/src/p1/p2/BaseBaseTest.java", "/edu.illinois.test/src/p1/p2/BaseInterface.java",
					"/edu.illinois.test/src/p1/p2/BaseBaseTestData.java", "/edu.illinois.test/src/p1/p2/MultipleParameters.java" };
		}
	}

	/**
	 * The sixth group includes refactorings: Introduce Indirection, Introduce Factory, Introduce
	 * Parameter, and Encapsulate Field.
	 * 
	 */
	public static class EclipseRefactoringsGroup6RecorderReplayerTest extends RecorderReplayerTest {
		@Override
		protected String getTestNumber() {
			return "17";
		}

		@Override
		protected String[] getTestFileNames() {
			return new String[] { "BaseTest2.java", "DerivedTest1.java", "DerivedTest2.java", "MyRunnable.java", "BaseBaseTest.java", "BaseInterface.java", "BaseBaseTestData.java",
					"MultipleParameters.java" };
		}

		@Override
		protected String[] getGeneratedFilePaths() {
			return new String[] { "/edu.illinois.test/src/p1/p2/BaseTest2.java", "/edu.illinois.test/src/p1/p2/DerivedTest1.java", "/edu.illinois.test/src/p1/p2/DerivedTest2.java",
					"/edu.illinois.test/src/p1/p2/MyRunnable.java", "/edu.illinois.test/src/p1/p2/BaseBaseTest.java", "/edu.illinois.test/src/p1/p2/BaseInterface.java",
					"/edu.illinois.test/src/p1/p2/BaseBaseTestData.java", "/edu.illinois.test/src/p1/p2/MultipleParameters.java" };
		}
	}

	/**
	 * The seventh group includes refactorings: Generalize Declared Type and Infer Generic Type
	 * Arguments.
	 * 
	 */
	public static class EclipseRefactoringsGroup7RecorderReplayerTest extends RecorderReplayerTest {
		@Override
		protected String getTestNumber() {
			return "18";
		}

		@Override
		protected String[] getTestFileNames() {
			return new String[] { "BaseTest2.java", "DerivedTest1.java", "DerivedTest2.java", "MyRunnable.java", "BaseBaseTest.java", "BaseInterface.java", "BaseBaseTestData.java",
					"MultipleParameters.java" };
		}

		@Override
		protected String[] getGeneratedFilePaths() {
			return new String[] { "/edu.illinois.test/src/p1/p2/BaseTest2.java", "/edu.illinois.test/src/p1/p2/DerivedTest1.java", "/edu.illinois.test/src/p1/p2/DerivedTest2.java",
					"/edu.illinois.test/src/p1/p2/MyRunnable.java", "/edu.illinois.test/src/p1/p2/BaseBaseTest.java", "/edu.illinois.test/src/p1/p2/BaseInterface.java",
					"/edu.illinois.test/src/p1/p2/BaseBaseTestData.java", "/edu.illinois.test/src/p1/p2/MultipleParameters.java" };
		}
	}

}
