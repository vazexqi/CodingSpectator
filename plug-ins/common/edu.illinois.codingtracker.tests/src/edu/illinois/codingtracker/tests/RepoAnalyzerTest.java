/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.junit.Test;

import edu.illinois.codingtracker.helpers.ResourceHelper;
import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.tests.postprocessors.ast.AddDeleteUpdateInferencePostprocessor;


/**
 * 
 * @author Stas Negara
 * 
 */
public class RepoAnalyzerTest {

	private static final String oldFileName= "OldFile.java";

	private static final String newFileName= "NewFile.java";


	@Test
	public void testOneChange() {
		File oldFile= new File(RecorderReplayerTest.TEST_FILES_FOLDER + "/" + "99" + "/" + oldFileName);
		File newFile= new File(RecorderReplayerTest.TEST_FILES_FOLDER + "/" + "99" + "/" + newFileName);
		String oldFileContent= ResourceHelper.readFileContent(oldFile);
		String newFileContent= ResourceHelper.readFileContent(newFile);
		try {
			Set<ASTOperation> operations= AddDeleteUpdateInferencePostprocessor.getDiffAsASTNodeOperations(oldFileContent, newFileContent);
			assertEquals(1, operations.size());
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

}
