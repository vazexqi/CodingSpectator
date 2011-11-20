/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.csvtotransactions;

import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.supercsv.io.CsvMapReader;
import org.supercsv.prefs.CsvPreference;

/**
 * 
 * @author Mohsen Vakilian
 * 
 */
public class CSVReader implements Iterable<Map<String, String>> {

	private String csvFilePath;

	private CsvMapReader reader;

	private String[] header;

	public CSVReader(String csvFilePath) {
		this.csvFilePath= csvFilePath;
	}

	private void readCSVHeader() throws IOException {
		if (reader == null) {
			reader= new CsvMapReader(new FileReader(csvFilePath), CsvPreference.EXCEL_PREFERENCE);
			header= reader.getCSVHeader(true);
		}
	}

	private boolean isInitialized() {
		return reader != null && header != null;
	}

	private Map<String, String> getNextRow() throws IOException {
		if (!isInitialized()) {
			readCSVHeader();
		}
		Map<String, String> row= reader.read(header);
		return row;
	}

	@Override
	public Iterator<Map<String, String>> iterator() {
		return new CSVRowIterator();
	}

	public class CSVRowIterator implements Iterator<Map<String, String>> {

		boolean hasComputedNext= false;

		Map<String, String> prospectiveNext= null;

		private void computeNext() {
			try {
				prospectiveNext= getNextRow();
				hasComputedNext= true;
			} catch (IOException e) {
				throw new RuntimeException("Failed to retrieve the next row.");
			}
		}

		@Override
		public boolean hasNext() {
			if (!hasComputedNext) {
				computeNext();
			}
			return prospectiveNext != null;
		}

		@Override
		public Map<String, String> next() {
			if (!hasComputedNext) {
				computeNext();
			}
			hasComputedNext= false;
			return prospectiveNext;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

}
