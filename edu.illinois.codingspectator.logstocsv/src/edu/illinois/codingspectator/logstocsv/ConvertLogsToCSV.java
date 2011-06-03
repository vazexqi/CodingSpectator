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
import edu.illinois.codingtracker.helpers.ResourceHelper;
import edu.illinois.codingtracker.operations.OperationDeserializer;
import edu.illinois.codingtracker.operations.UserOperation;

/**
 * 
 * @author Mohsen Vakilian
 * @author nchen
 * 
 */
public class ConvertLogsToCSV {

	public static void main(String[] args) throws CoreException, IOException {
		EFSFile root= new EFSFile(args[1]);
		String CSVFileName= args[2];

		Collection<AbstractMapWrapper> mapWrappers= extractMapWrappers(root);
		writeMapWrappersToCSV(mapWrappers, CSVFileName);
	}


	private static void writeMapWrappersToCSV(Collection<AbstractMapWrapper> mapWrappers, String fileName) throws IOException {
		CsvMapWriter csvwriter= new CsvMapWriter(new FileWriter(fileName), CsvPreference.EXCEL_PREFERENCE);

		Set<String> attributeKeys= new HashSet<String>();
		for (AbstractMapWrapper mapWrapper : mapWrappers) {
			attributeKeys.addAll(mapWrapper.toMap().keySet());
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
		for (AbstractMapWrapper mapWrapper : mapWrappers) {
			csvwriter.write(mapWrapper.toMap(), columnNames, cellProcessors);
		}
		csvwriter.close();
	}

	private static Collection<AbstractMapWrapper> extractMapWrappers(EFSFile root) throws CoreException {
		Collection<AbstractMapWrapper> refactoringDescriptors= new ArrayList<AbstractMapWrapper>();
		for (EFSFile usernameFolder : root.children()) {
			for (EFSFile workspaceFolder : usernameFolder.children()) {
				for (EFSFile versionFolder : workspaceFolder.children()) {
					String username= usernameFolder.getPath().lastSegment();
					String workspaceID= workspaceFolder.getPath().lastSegment();
					IPath codingSpectatorVersionPath= versionFolder.getPath();
					String codingspectatorVersion= codingSpectatorVersionPath.lastSegment();

					IPath codingspectatorRefactoringsPath= codingSpectatorVersionPath.append("refactorings");
					refactoringDescriptors.addAll(getRefactoringDescriptors(codingSpectatorVersionPath, LogType.ECLIPSE, username, workspaceID, codingspectatorVersion));
					refactoringDescriptors.addAll(getRefactoringDescriptors(codingspectatorRefactoringsPath, LogType.CANCELLED, username, workspaceID, codingspectatorVersion));
					refactoringDescriptors.addAll(getRefactoringDescriptors(codingspectatorRefactoringsPath, LogType.PERFORMED, username, workspaceID, codingspectatorVersion));
					refactoringDescriptors.addAll(getRefactoringDescriptors(codingspectatorRefactoringsPath, LogType.UNAVAILABLE, username, workspaceID, codingspectatorVersion));

					IPath codingtrackerPath= codingSpectatorVersionPath.append("codingtracker").append("codechanges.txt");
					refactoringDescriptors.addAll(getUserOperations(codingtrackerPath, username, workspaceID, codingspectatorVersion));

				}
			}
		}
		return refactoringDescriptors;
	}

	private static Collection<AbstractMapWrapper> getUserOperations(IPath codingtrackerPath, String username, String workspaceID, String codingspectatorVersion) {
		String operationsRecord= ResourceHelper.readFileContent(codingtrackerPath.toFile());
		Collection<UserOperation> userOperations= OperationDeserializer.getUserOperations(operationsRecord);
		return toUserOperationMapWrappers(username, workspaceID, codingspectatorVersion, userOperations);
	}


	private static Collection<AbstractMapWrapper> toUserOperationMapWrappers(String username, String workspaceID, String codingspectatorVersion, Collection<UserOperation> userOperations) {
		Collection<AbstractMapWrapper> userOperationsWrapper= new ArrayList<AbstractMapWrapper>();
		for (UserOperation userOperation : userOperations) {
			UserOperationMapWrapper userOperationMapWrapper= new UserOperationMapWrapper(userOperation, username, workspaceID, codingspectatorVersion);
			if (userOperationMapWrapper.shouldBeIncludedInCSV())
				userOperationsWrapper.add(userOperationMapWrapper);
		}
		return userOperationsWrapper;
	}

	private static Collection<RefactoringDescriptorMapWrapper> getRefactoringDescriptors(IPath codingspectatorRefactoringsPath, LogType type, String username,
			String workspaceID, String codingspectatorVersion) throws CoreException {
		String refactoringHistoryFolder= type == LogType.ECLIPSE ? "eclipse-refactorings" : RefactoringLog.toString(type);
		RefactoringLog refactoringLog= new RefactoringLog(codingspectatorRefactoringsPath.append(refactoringHistoryFolder));
		Collection<CapturedRefactoringDescriptor> refactoringDescriptors= refactoringLog.getRefactoringDescriptors();
		return toRefactoringDescriptorMapWrappers(refactoringDescriptors, username, workspaceID, codingspectatorVersion, type.toString());
	}

	private static Collection<RefactoringDescriptorMapWrapper> toRefactoringDescriptorMapWrappers(Collection<CapturedRefactoringDescriptor> capturedRefactoringDescriptors, String username,
			String workspaceID, String codingspectatorVersion, String refactoringKind) {
		Collection<RefactoringDescriptorMapWrapper> refactoringDescriptors= new ArrayList<RefactoringDescriptorMapWrapper>();
		for (CapturedRefactoringDescriptor capturedRefactoringDescriptor : capturedRefactoringDescriptors) {
			refactoringDescriptors.add(new RefactoringDescriptorMapWrapper(capturedRefactoringDescriptor, username, workspaceID, codingspectatorVersion, refactoringKind));
		}
		return refactoringDescriptors;
	}
}
