/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers.methods;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import edu.illinois.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.operations.ast.ASTOperation;
import edu.illinois.codingtracker.tests.analyzers.CSVProducingAnalyzer;


/**
 * This analyzer calculates how many changes are performed for methods that are changed at least
 * once.
 * 
 * @author Stas Negara
 * 
 */
public class PlainMethodChangesAnalyzer extends CSVProducingAnalyzer {

	private final Map<Long, Integer> changesCounter= new HashMap<Long, Integer>();

	private final Map<String, Set<Long>> sortedMethodNames= new TreeMap<String, Set<Long>>();


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
			addMethodName(astOperation.getMethodName(), methodID);
			Integer currentChangesCount= changesCounter.get(methodID);
			int newChangesCount= currentChangesCount == null ? 1 : currentChangesCount + 1;
			changesCounter.put(methodID, newChangesCount);
		}
	}

	private void addMethodName(String methodName, long methodID) {
		Set<Long> methodIDs= sortedMethodNames.get(methodName);
		if (methodIDs == null) {
			methodIDs= new HashSet<Long>();
			sortedMethodNames.put(methodName, methodIDs);
		}
		methodIDs.add(methodID);
	}

	private void populateResults() {
		for (Entry<String, Set<Long>> mapEntry : sortedMethodNames.entrySet()) {
			String methodName= mapEntry.getKey();
			for (long methodID : mapEntry.getValue()) {
				Integer changesCount= changesCounter.get(methodID);
				appendCSVEntry(postprocessedUsername, postprocessedWorkspaceID, methodID, methodName, changesCount);
			}
		}
	}

	private void initialize() {
		result= new StringBuffer();
		changesCounter.clear();
		sortedMethodNames.clear();
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
