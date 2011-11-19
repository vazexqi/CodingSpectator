/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.operations.junit.TestCaseFinishedOperation;
import edu.illinois.codingtracker.operations.junit.TestCaseStartedOperation;


/**
 * This analyzer calculates how many failing tests are made passing, how many of such test fixes
 * involved changing the tests, and what is the ratio.
 * 
 * @author Stas Negara
 * 
 */
public class TestFixesAnalyzer extends CSVProducingAnalyzer {

	private final Set<String> failingTests= new HashSet<String>();

	private final Set<String> failingChangedTests= new HashSet<String>();

	private String currentTestMethodName;

	private int fixedTestsCount;

	private int fixedChangedTestsCount;


	@Override
	protected String getTableHeader() {
		return "username,workspace ID,fixed tests count,fixed and changed tests count,ratio (%)\n";
	}

	@Override
	protected void checkPostprocessingPreconditions() {
		//no preconditions
	}

	@Override
	protected boolean shouldPostprocessVersionFolder(String folderName) {
		return true;
	}

	@Override
	protected String getRecordFileName() {
		return "codechanges.txt.inferred_ast_operations";
	}

	@Override
	protected void postprocess(List<UserOperation> userOperations) {
		initialize();
		for (UserOperation userOperation : userOperations) {
			if (userOperation instanceof TestCaseStartedOperation) {
				handleTestCaseStartedOperation((TestCaseStartedOperation)userOperation);
			} else if (userOperation instanceof TestCaseFinishedOperation) {
				handleTestCaseFinishedOperation((TestCaseFinishedOperation)userOperation);
			} else if (userOperation instanceof ASTOperation) {
				handleASTOperation((ASTOperation)userOperation);
			}
		}
		populateResults();
		System.out.println("Fixed tests count: " + fixedTestsCount);
		System.out.println("Fixed changed tests count: " + fixedChangedTestsCount);
	}

	private void handleTestCaseStartedOperation(TestCaseStartedOperation testCaseStartedOperation) {
		currentTestMethodName= testCaseStartedOperation.getFullyQualifiedMethodName();
	}

	private void handleTestCaseFinishedOperation(TestCaseFinishedOperation testCaseFinishedOperation) {
		//Note that an "Ignored" test is neither failed nor passed.
		if (testCaseFinishedOperation.hasFailed()) {
			failingTests.add(currentTestMethodName);
		} else if (testCaseFinishedOperation.hasPassed() && failingTests.contains(currentTestMethodName)) {
			failingTests.remove(currentTestMethodName);
			fixedTestsCount++;
			if (failingChangedTests.contains(currentTestMethodName)) {
				failingChangedTests.remove(currentTestMethodName);
				fixedChangedTestsCount++;
			}
		}
	}

	private void handleASTOperation(ASTOperation astOperation) {
		if (astOperation.getMethodID() != -1) { //Check if there is a containing method.
			String methodPackageName= getPackageName(astOperation.getMethodName());
			for (String failingTest : failingTests) {
				if (methodPackageName.startsWith(getPackageName(failingTest))) {
					failingChangedTests.add(failingTest);
				}
			}
		}
	}

	private void populateResults() {
		double ratio= (double)fixedChangedTestsCount * 100 / fixedTestsCount;
		appendCSVEntry(postprocessedUsername, postprocessedWorkspaceID, fixedTestsCount, fixedChangedTestsCount,
						Math.round(ratio));
	}

	private void initialize() {
		result= new StringBuffer();
		failingTests.clear();
		failingChangedTests.clear();
		fixedTestsCount= 0;
		fixedChangedTestsCount= 0;
	}

	private String getPackageName(String methodName) {
		String separator= ".";
		int packageNameEndIndex= methodName.lastIndexOf(separator, methodName.lastIndexOf(separator) - 1);
		//Add 1 to the package name end index to get the trailing separator such that it is easy to check whether 
		//one package is inside the other one by comparing their names.
		return methodName.substring(0, packageNameEndIndex + 1);
	}

	@Override
	protected String getResultFilePostfix() {
		return ".test_fixes";
	}

	@Override
	protected boolean shouldMergeResults() {
		return true;
	}

}
