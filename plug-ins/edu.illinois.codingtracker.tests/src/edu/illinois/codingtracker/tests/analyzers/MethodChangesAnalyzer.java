/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.ast.ASTOperation;


/**
 * This analyzer calculates how many changes are performed for methods that are changed at least
 * once.
 * 
 * @author Stas Negara
 * 
 */
public class MethodChangesAnalyzer extends CSVProducingAnalyzer {

	private final Map<Long, Integer> changesCounter= new HashMap<Long, Integer>();

	private final Map<Long, String> methodInfo= new HashMap<Long, String>();


	@Override
	protected String getTableHeader() {
		return "username,workspace ID,method ID,method name,changes count\n";
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
		long methodID= astOperation.getMethodID();
		if (methodID != -1) { //Check if there is a containing method.
			if (!methodInfo.containsKey(methodID)) {
				methodInfo.put(methodID, astOperation.getMethodName());
				changesCounter.put(methodID, 1);
			} else {
				Integer currentChangesCount= changesCounter.get(methodID);
				int newChangesCount= currentChangesCount + 1;
				changesCounter.put(methodID, newChangesCount);
			}
		}
	}

	private void populateResults() {
		for (Entry<Long, Integer> mapEntry : changesCounter.entrySet()) {
			Long methodID= mapEntry.getKey();
			Integer changesCount= mapEntry.getValue();
			appendCSVEntry(postprocessedUsername, postprocessedWorkspaceID, methodID, methodInfo.get(methodID), changesCount);
		}
	}

	private void initialize() {
		result= new StringBuffer();
		changesCounter.clear();
		methodInfo.clear();
	}

	@Override
	protected String getResultFilePostfix() {
		return ".method_changes";
	}

	@Override
	protected boolean shouldMergeResults() {
		return true;
	}

}
