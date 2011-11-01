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
 * This analyzer is a base class for all analyzers that calculate how many changes are performed for
 * methods aggregating them by a particular criterion.
 * 
 * @author Stas Negara
 * 
 */
public abstract class AggregatedMethodChangesAnalyzer extends CSVProducingAnalyzer {

	private static final int ratioNormalizationFactor= 100;

	private final Map<Integer, Integer> changesCounter= new TreeMap<Integer, Integer>();

	private final Map<Integer, Set<Long>> changedMethodsCounter= new HashMap<Integer, Set<Long>>();

	private int totalMethodChangesCount;


	@Override
	protected String getTableHeader() {
		String tableTitle= "username,workspace ID," + getAggregatedColumnTitle();
		tableTitle+= ",changed methods count,changes count,average changes count,total changed methods count,total changes count,total average changes count,average ratio (%)\n";
		return tableTitle;
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
			totalMethodChangesCount++;
			int aggregatedValue= getAggregatedValue(astOperation);
			addChangedMethod(methodID, aggregatedValue);
			Integer currentChangesCount= changesCounter.get(aggregatedValue);
			int newChangesCount= currentChangesCount == null ? 1 : currentChangesCount + 1;
			changesCounter.put(aggregatedValue, newChangesCount);
		}
	}

	private void addChangedMethod(long methodID, int aggregatedValue) {
		Set<Long> changedMethodIDs= changedMethodsCounter.get(aggregatedValue);
		if (changedMethodIDs == null) {
			changedMethodIDs= new HashSet<Long>();
			changedMethodsCounter.put(aggregatedValue, changedMethodIDs);
		}
		changedMethodIDs.add(methodID);
	}

	private void populateResults() {
		int totalChangedMethodsCount= 0;
		for (Set<Long> changedMethods : changedMethodsCounter.values()) {
			totalChangedMethodsCount+= changedMethods.size();
		}
		double totalAverageChangesCount= (double)totalMethodChangesCount / totalChangedMethodsCount;
		for (Entry<Integer, Integer> mapEntry : changesCounter.entrySet()) {
			Integer aggregatedValue= mapEntry.getKey();
			Integer methodChangesCount= mapEntry.getValue();
			int changedMethodsCount= changedMethodsCounter.get(aggregatedValue).size();
			double averageChangesCount= (double)methodChangesCount / changedMethodsCount;
			double changeRatio= averageChangesCount * ratioNormalizationFactor / totalAverageChangesCount;
			appendCSVEntry(postprocessedUsername, postprocessedWorkspaceID, aggregatedValue, changedMethodsCount,
					methodChangesCount, Math.round(averageChangesCount), totalChangedMethodsCount, totalMethodChangesCount,
					Math.round(totalAverageChangesCount), Math.round(changeRatio));
		}
	}

	private void initialize() {
		result= new StringBuffer();
		changesCounter.clear();
		changedMethodsCounter.clear();
		totalMethodChangesCount= 0;
	}

	@Override
	protected boolean shouldMergeResults() {
		return true;
	}

	protected abstract String getAggregatedColumnTitle();

	protected abstract int getAggregatedValue(ASTOperation astOperation);

}
