/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.logstocsv;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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

	private static Collection<RefactoringDescriptorMapWrapper> toRefactoringDescriptorMapWrappers(Collection<CapturedRefactoringDescriptor> capturedRefactoringDescriptors, String username,
			String workspaceID, String codingspectatorVersion, String refactoringKind) {
		Collection<RefactoringDescriptorMapWrapper> refactoringDescriptors= new ArrayList<RefactoringDescriptorMapWrapper>();
		for (CapturedRefactoringDescriptor capturedRefactoringDescriptor : capturedRefactoringDescriptors) {
			refactoringDescriptors.add(new RefactoringDescriptorMapWrapper(capturedRefactoringDescriptor, username, workspaceID, codingspectatorVersion, refactoringKind));
		}
		return refactoringDescriptors;
	}

	private static Collection<RefactoringDescriptorMapWrapper> getRefactoringDescriptors(IPath codingspectatorRefactoringsPath, LogType type, String username,
			String workspaceID, String codingspectatorVersion) throws CoreException {
		RefactoringLog refactoringLog= new RefactoringLog(codingspectatorRefactoringsPath.append(RefactoringLog.toString(type)));
		Collection<CapturedRefactoringDescriptor> refactoringDescriptors= refactoringLog.getRefactoringDescriptors();
		return toRefactoringDescriptorMapWrappers(refactoringDescriptors, username, workspaceID, codingspectatorVersion, type.toString());
	}

	public static void main(String[] args) throws CoreException, IOException {
		EFSFile root= new EFSFile(args[1]);

		Collection<RefactoringDescriptorMapWrapper> refactoringDescriptors= new ArrayList<RefactoringDescriptorMapWrapper>();
		for (EFSFile usernameFolder : root.children()) {
			for (EFSFile workspaceFolder : usernameFolder.children()) {
				for (EFSFile versionFolder : workspaceFolder.children()) {
					String username= usernameFolder.getPath().lastSegment();
					String workspaceID= workspaceFolder.getPath().lastSegment();
					String codingspectatorVersion= versionFolder.getPath().lastSegment();

					IPath codingspectatorRefactoringsPath= versionFolder.getPath().append("refactorings");
					refactoringDescriptors.addAll(getRefactoringDescriptors(codingspectatorRefactoringsPath, LogType.CANCELLED, username, workspaceID, codingspectatorVersion));
					refactoringDescriptors.addAll(getRefactoringDescriptors(codingspectatorRefactoringsPath, LogType.PERFORMED, username, workspaceID, codingspectatorVersion));
					refactoringDescriptors.addAll(getRefactoringDescriptors(codingspectatorRefactoringsPath, LogType.UNAVAILABLE, username, workspaceID, codingspectatorVersion));
				}
			}
		}

		CsvMapWriter csvwriter= new CsvMapWriter(new FileWriter(args[2]), CsvPreference.EXCEL_PREFERENCE);

		Set<String> attributeKeys= new HashSet<String>();
		for (RefactoringDescriptorMapWrapper refactoringDescriptor : refactoringDescriptors) {
			attributeKeys.addAll(refactoringDescriptor.toMap().keySet());
		}
		String[] columnNames= attributeKeys.toArray(new String[] {});
		Arrays.sort(columnNames);
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
		for (RefactoringDescriptorMapWrapper refactoringDescriptor : refactoringDescriptors) {
			csvwriter.write(refactoringDescriptor.toMap(), columnNames, cellProcessors);
		}
		csvwriter.close();
	}
}
