/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers.methods;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.tests.analyzers.CSVProducingAnalyzer;


/**
 * This analyzer calculates how many changes are performed for methods aggregating them by
 * cyclomatic complexity.
 * 
 * @author Stas Negara
 * 
 */
public class MethodChangesByComplexityAnalyzer extends CSVProducingAnalyzer {

	private final Map<Integer, Integer> changesCounter= new TreeMap<Integer, Integer>();


	@Override
	protected String getTableHeader() {
		return "username,workspace ID,cyclomatic complexity,changes count\n";
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
			if (userOperation instanceof ASTOperation) {
				handleASTOperation((ASTOperation)userOperation);
			}
		}
		populateResults();
	}

	private void handleASTOperation(ASTOperation astOperation) {
		if (astOperation.getMethodID() != -1) { //Check if there is a containing method.
			int methodCyclomaticComplexity= astOperation.getMethodCyclomaticComplexity();
			Integer currentChangesCount= changesCounter.get(methodCyclomaticComplexity);
			int newChangesCount= currentChangesCount == null ? 1 : currentChangesCount + 1;
			changesCounter.put(methodCyclomaticComplexity, newChangesCount);
		}
	}

	private void populateResults() {
		for (Entry<Integer, Integer> mapEntry : changesCounter.entrySet()) {
			appendCSVEntry(postprocessedUsername, postprocessedWorkspaceID, mapEntry.getKey(), mapEntry.getValue());
		}
	}

	private void initialize() {
		result= new StringBuffer();
		changesCounter.clear();
	}

	@Override
	protected String getResultFilePostfix() {
		return ".method_changes_complexity";
	}

	@Override
	protected boolean shouldMergeResults() {
		return false;
	}

}
