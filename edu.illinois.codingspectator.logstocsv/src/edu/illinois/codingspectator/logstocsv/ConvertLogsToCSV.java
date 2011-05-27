/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.logstocsv;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.prefs.CsvPreference;
import org.supercsv.util.CSVContext;

import edu.illinois.codingspectator.efs.EFSFile;
import edu.illinois.codingspectator.refactorings.parser.CapturedRefactoringDescriptor;
import edu.illinois.codingspectator.refactorings.parser.RefactoringLog;
import edu.illinois.codingspectator.refactorings.parser.RefactoringLog.LogType;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class ConvertLogsToCSV {

	public static void main(String[] args) throws CoreException, IOException {
		String pathToUserFolder= args[1];
		EFSFile userFolder= new EFSFile(pathToUserFolder);

		Collection<CapturedRefactoringDescriptor> refactoringDescriptors= new ArrayList<CapturedRefactoringDescriptor>();
		for (EFSFile workspace : userFolder.children()) {
			for (EFSFile version : workspace.children()) {
				IPath refactoringsPath= version.getPath().append("refactorings");
				RefactoringLog canceledRefactoringLog= new RefactoringLog(refactoringsPath.append(RefactoringLog.toString(LogType.CANCELLED)));
				refactoringDescriptors.addAll(canceledRefactoringLog.getRefactoringDescriptors());

				RefactoringLog performedRefactoringLog= new RefactoringLog(refactoringsPath.append(RefactoringLog.toString(LogType.PERFORMED)));
				refactoringDescriptors.addAll(performedRefactoringLog.getRefactoringDescriptors());

				RefactoringLog unavailableRefactoringLog= new RefactoringLog(refactoringsPath.append(RefactoringLog.toString(LogType.UNAVAILABLE)));
				refactoringDescriptors.addAll(unavailableRefactoringLog.getRefactoringDescriptors());

				RefactoringLog eclipseRefactoringLog= new RefactoringLog(refactoringsPath.append(RefactoringLog.toString(LogType.ECLIPSE)));
				refactoringDescriptors.addAll(eclipseRefactoringLog.getRefactoringDescriptors());

			}
		}

		CsvMapWriter csvwriter= new CsvMapWriter(new FileWriter(args[2]), CsvPreference.EXCEL_PREFERENCE);

		Set<String> attributeKeys= new HashSet<String>();
		for (CapturedRefactoringDescriptor capturedRefactoringDescriptor : refactoringDescriptors) {
			attributeKeys.addAll(capturedRefactoringDescriptor.getAttributeKeys());
		}
		String[] columnNames= attributeKeys.toArray(new String[] {});
		csvwriter.writeHeader(columnNames);

		CellProcessor cellProcessor= new CellProcessor() {

			@Override
			public Object execute(Object value, CSVContext context) {
				if (value == null) {
					return "";
				} else
					return value;
			}
		};

		CellProcessor[] cellProcessors= new CellProcessor[columnNames.length];
		for (int i= 0; i < cellProcessors.length; i++) {
			cellProcessors[i]= cellProcessor;
		}
		for (CapturedRefactoringDescriptor capturedRefactoringDescriptor : refactoringDescriptors) {
			csvwriter.write(capturedRefactoringDescriptor.getArguments(), columnNames, cellProcessors);
		}
		csvwriter.close();
	}
}
