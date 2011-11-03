/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers.methods;

import edu.illinois.codingtracker.operations.ast.ASTOperation;


/**
 * This analyzer calculates how many refactorings are performed for methods aggregating them by size
 * (lines count).
 * 
 * @author Stas Negara
 * 
 */
public class MethodRefactoringsBySizeAnalyzer extends AggregatedMethodRefactoringsAnalyzer {


	@Override
	protected String getResultFilePostfix() {
		return ".method_refactorings_size";
	}

	@Override
	protected String getAggregatedColumnTitle() {
		return "lines count";
	}

	@Override
	protected int getAggregatedValue(ASTOperation astOperation) {
		return astOperation.getMethodLinesCount();
	}

}
