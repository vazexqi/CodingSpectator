/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers.methods;

import edu.illinois.codingtracker.operations.ast.ASTOperation;


/**
 * This analyzer calculates how many changes are performed for methods aggregating them by
 * cyclomatic complexity.
 * 
 * @author Stas Negara
 * 
 */
public class MethodChangesByComplexityAnalyzer extends AggregatedMethodChangesAnalyzer {


	@Override
	protected String getResultFilePostfix() {
		return ".method_changes_complexity";
	}

	@Override
	protected String getAggregatedColumnTitle() {
		return "cyclomatic complexity";
	}

	@Override
	protected int getAggregatedValue(ASTOperation astOperation) {
		return astOperation.getMethodCyclomaticComplexity();
	}

}
