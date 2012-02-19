/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.tests.analyzers;

import java.io.File;

import edu.illinois.codingtracker.tests.postprocessors.CodingTrackerPostprocessor;


/**
 * This is a base class for analyzers that produce CSV files as their output.
 * 
 * @author Stas Negara
 * 
 */
public abstract class CSVProducingAnalyzer extends CodingTrackerPostprocessor {

	protected StringBuffer result;


	protected void appendCSVEntry(Object... values) {
		for (int i= 0; i < values.length - 1; i++) {
			result.append(values[i]).append(",");
		}
		result.append(values[values.length - 1]).append("\n");
	}

	@Override
	protected String getResult() {
		return getTableHeader() + result.toString();
	}

	@Override
	protected String getResultToMerge() {
		return result.toString();
	}

	@Override
	protected String getMergedFilePrefix() {
		return getTableHeader();
	}

	@Override
	protected void handleFileDataInitializationException(File file, Exception e) {
		throw new RuntimeException("Wrong preprocessor root folder: can not initialize username, workspace ID, or version for file: "
									+ file.getName(), e);
	}

	protected abstract String getTableHeader();

}
