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
import edu.illinois.codingtracker.operations.refactorings.FinishedRefactoringOperation;
import edu.illinois.codingtracker.operations.refactorings.NewStartedRefactoringOperation;
import edu.illinois.codingtracker.tests.analyzers.CSVProducingAnalyzer;


/**
 * This analyzer is a base class for all analyzers that calculate how many refactorings are
 * performed for methods aggregating them by a particular criterion.
 * 
 * TODO: This class has a lot in common with AggregatedMethodChangesAnalyzer. Consider factoring out
 * common parts.
 * 
 * @author Stas Negara
 * 
 */
public abstract class AggregatedMethodRefactoringsAnalyzer extends CSVProducingAnalyzer {

	private static final int averageNormalizationFactor= 100;

	private static final int ratioNormalizationFactor= 100;

	private final Map<Integer, Integer> refactoringsCounter= new TreeMap<Integer, Integer>();

	private final Map<Integer, Set<Long>> changedMethodsCounter= new HashMap<Integer, Set<Long>>();

	private final Map<Integer, Set<Long>> perRefactoringChangedMethodsCounter= new HashMap<Integer, Set<Long>>();

	private int totalMethodRefactoringsCount;


	@Override
	protected String getTableHeader() {
		String tableTitle= "username,workspace ID," + getAggregatedColumnTitle();
		tableTitle+= ",changed methods count,refactorings count,average refactorings count,total changed methods count,total refactorings count,total average refactorings count,average ratio (%)\n";
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
			if (userOperation instanceof NewStartedRefactoringOperation) {
				perRefactoringChangedMethodsCounter.clear();
			} else if (userOperation instanceof FinishedRefactoringOperation) {
				aggregatePerRefactoringChanges();
			} else if (userOperation instanceof ASTOperation) {
				handleASTOperation((ASTOperation)userOperation);
			}
		}
		populateResults();
	}

	private void aggregatePerRefactoringChanges() {
		for (Entry<Integer, Set<Long>> mapEntry : perRefactoringChangedMethodsCounter.entrySet()) {
			int aggregatedValue= mapEntry.getKey();
			int increment= mapEntry.getValue().size();
			Integer currentRefactoringsCount= refactoringsCounter.get(aggregatedValue);
			int newRefactoringsCount= currentRefactoringsCount == null ? increment : currentRefactoringsCount + increment;
			refactoringsCounter.put(aggregatedValue, newRefactoringsCount);
			totalMethodRefactoringsCount+= increment;
		}
	}

	private void handleASTOperation(ASTOperation astOperation) {
		long methodID= astOperation.getMethodID();
		if (methodID != -1) { //Check if there is a containing method.
			int aggregatedValue= getAggregatedValue(astOperation);
			addChangedMethod(methodID, aggregatedValue);
		}
	}

	private void addChangedMethod(long methodID, int aggregatedValue) {
		addChangedMethod(changedMethodsCounter, methodID, aggregatedValue);
		addChangedMethod(perRefactoringChangedMethodsCounter, methodID, aggregatedValue);
	}

	private void addChangedMethod(Map<Integer, Set<Long>> changedMethodsCounter, long methodID, int aggregatedValue) {
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
		double totalAverageRefactoringsCount= (double)totalMethodRefactoringsCount * averageNormalizationFactor / totalChangedMethodsCount;
		for (Entry<Integer, Integer> mapEntry : refactoringsCounter.entrySet()) {
			Integer aggregatedValue= mapEntry.getKey();
			Integer methodRefactoringsCount= mapEntry.getValue();
			int changedMethodsCount= changedMethodsCounter.get(aggregatedValue).size();
			double averageRefactoringsCount= (double)methodRefactoringsCount * averageNormalizationFactor / changedMethodsCount;
			double refactoringRatio= averageRefactoringsCount * ratioNormalizationFactor / totalAverageRefactoringsCount;
			appendCSVEntry(postprocessedUsername, postprocessedWorkspaceID, aggregatedValue, changedMethodsCount,
					methodRefactoringsCount, Math.round(averageRefactoringsCount), totalChangedMethodsCount,
					totalMethodRefactoringsCount, Math.round(totalAverageRefactoringsCount), Math.round(refactoringRatio));
		}
	}

	private void initialize() {
		result= new StringBuffer();
		refactoringsCounter.clear();
		changedMethodsCounter.clear();
		totalMethodRefactoringsCount= 0;
	}

	@Override
	protected boolean shouldMergeResults() {
		return true;
	}

	protected abstract String getAggregatedColumnTitle();

	protected abstract int getAggregatedValue(ASTOperation astOperation);

}
