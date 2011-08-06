/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.logstocsv;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.util.CSVContext;

/**
 * @author Mohsen Vakilian
 * 
 */
public class CodingSpectatorCSVWriter {

	String csvfileName;

	public CodingSpectatorCSVWriter(String csvFileName) {
		this.csvfileName= csvFileName;
	}

	public void writeEventToCSV(Collection<? extends Mappable> mappables) throws IOException {
		CsvMapWriter csvwriter= new CsvMapWriter(new FileWriter(csvfileName), CsvPreference.EXCEL_PREFERENCE);

		Set<String> attributeKeys= new HashSet<String>();
		for (Mappable mappable : mappables) {
			attributeKeys.addAll(mappable.toMap().keySet());
		}
		String[] columnNames= attributeKeys.toArray(new String[] {});
		Arrays.sort(columnNames);
		csvwriter.writeHeader(columnNames);

		CellProcessor cellProcessor= new CellProcessor() {

			@Override
			public Object execute(Object value, CSVContext context) {
				if (value == null) {
					return "";
				} else {
					return value;
				}
			}
		};

		CellProcessor[] cellProcessors= new CellProcessor[columnNames.length];
		for (int i= 0; i < cellProcessors.length; i++) {
			cellProcessors[i]= cellProcessor;
		}
		for (Mappable mappable : mappables) {
			csvwriter.write(mappable.toMap(), columnNames, cellProcessors);
		}
		csvwriter.close();
	}

}
