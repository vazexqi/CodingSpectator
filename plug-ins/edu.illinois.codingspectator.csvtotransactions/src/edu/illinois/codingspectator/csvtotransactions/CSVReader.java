/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.csvtotransactions;

import java.io.IOException;
import java.io.Reader;
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

	private CsvMapReader csvMapReader;

	private String[] csvHeader;

	public CSVReader(Reader reader) {
		csvMapReader= new CsvMapReader(reader, CsvPreference.EXCEL_PREFERENCE);
	}

	private void readHeader() throws IOException {
		csvHeader= csvMapReader.getCSVHeader(true);
	}

	private Map<String, String> getNextRow() throws IOException {
		return csvMapReader.read(csvHeader);
	}

	@Override
	public Iterator<Map<String, String>> iterator() {
		return new CSVRowIterator();
	}

	public class CSVRowIterator implements Iterator<Map<String, String>> {

		boolean hasReadHeader= false;

		boolean hasComputedNext= false;

		Map<String, String> prospectiveNext= null;

		private void computeNext() {
			try {
				if (!hasReadHeader) {
					readHeader();
					hasReadHeader= true;
				}
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
