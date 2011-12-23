/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.logstocsv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import edu.illinois.codingspectator.efs.EFSFile;
import edu.illinois.codingspectator.refactorings.parser.CapturedRefactoringDescriptor;
import edu.illinois.codingspectator.refactorings.parser.RefactoringLog;
import edu.illinois.codingspectator.refactorings.parser.RefactoringLog.LogType;
import edu.illinois.codingtracker.helpers.ResourceHelper;
import edu.illinois.codingtracker.operations.OperationDeserializer;
import edu.illinois.codingtracker.operations.UserOperation;

/**
 * @author Mohsen Vakilian
 * 
 */
public class CodingSpectatorDataExtractor {

	private EFSFile rootDataFolder;

	public CodingSpectatorDataExtractor(EFSFile rootDataFolder) {
		this.rootDataFolder= rootDataFolder;
	}

	public Collection<Event> extractData() throws CoreException {
		Collection<Event> operations= new ArrayList<Event>();
		for (EFSFile usernameFolder : usersUnderStudy()) {
			for (EFSFile workspaceFolder : CodingSpectatorDataExtractor.childrenExceptSVNFolders(usernameFolder)) {
				for (EFSFile versionFolder : CodingSpectatorDataExtractor.versionsUnderStudy(workspaceFolder)) {
					String username= usernameFolder.getPath().lastSegment();
					String workspaceID= workspaceFolder.getPath().lastSegment();
					IPath codingSpectatorVersionPath= versionFolder.getPath();
					String codingspectatorVersion= codingSpectatorVersionPath.lastSegment();

					IPath codingspectatorRefactoringsPath= codingSpectatorVersionPath.append("refactorings");
					operations.addAll(CodingSpectatorDataExtractor.getRefactoringDescriptors(codingSpectatorVersionPath, LogType.ECLIPSE, username, workspaceID, codingspectatorVersion));
					operations.addAll(CodingSpectatorDataExtractor.getRefactoringDescriptors(codingspectatorRefactoringsPath, LogType.CANCELLED, username, workspaceID, codingspectatorVersion));
					operations.addAll(CodingSpectatorDataExtractor.getRefactoringDescriptors(codingspectatorRefactoringsPath, LogType.PERFORMED, username, workspaceID, codingspectatorVersion));
					operations.addAll(CodingSpectatorDataExtractor.getRefactoringDescriptors(codingspectatorRefactoringsPath, LogType.UNAVAILABLE, username, workspaceID, codingspectatorVersion));

					IPath codingtrackerPath= codingSpectatorVersionPath.append("codingtracker").append("codechanges.txt");

					if (new EFSFile(codingtrackerPath).exists()) {
						operations.addAll(CodingSpectatorDataExtractor.getUserOperations(codingtrackerPath, username, workspaceID, codingspectatorVersion));
					} else {
						System.err.println(String.format("CodingTracker's log at \"%s\" is missing.", codingtrackerPath.toOSString()));
					}
				}
			}
		}
		return operations;
	}

	private List<EFSFile> usersUnderStudy() throws CoreException {
		List<String> fileNames= rootDataFolder.childNames();
		List<EFSFile> filteredFiles= new ArrayList<EFSFile>();
		//		String regex= "cs-\\d\\d\\d";
		String regex= "cs-.*";
		Pattern userUnderStudyPattern= Pattern.compile(regex);
		for (String fileName : fileNames) {
			if (userUnderStudyPattern.matcher(fileName).matches())
				filteredFiles.add(rootDataFolder.append(fileName));
		}
		return filteredFiles;
	}

	private static Collection<Event> getUserOperations(IPath codingtrackerPath, String username, String workspaceID, String codingspectatorVersion) {
		String operationsRecord= ResourceHelper.readFileContent(codingtrackerPath.toFile());
		Collection<UserOperation> userOperations= new ArrayList<UserOperation>();
		try {
			userOperations= OperationDeserializer.getUserOperations(operationsRecord);
		} catch (RuntimeException e) {
			System.err.println(String.format("Failed to parse CodingTracker's log at \"%s\".", codingtrackerPath.toOSString()));
		}
		return CodingSpectatorDataExtractor.toUserOperationEvents(username, workspaceID, codingspectatorVersion, userOperations);
	}

	private static Collection<Event> toUserOperationEvents(String username, String workspaceID, String codingspectatorVersion, Collection<UserOperation> userOperations) {
		Collection<Event> userOperationsWrapper= new ArrayList<Event>();
		for (UserOperation userOperation : userOperations) {
			UserOperationEvent userOperationMapWrapper= new UserOperationEvent(userOperation, username, workspaceID, codingspectatorVersion);
			if (userOperationMapWrapper.shouldBeIncludedInCSV())
				userOperationsWrapper.add(userOperationMapWrapper);
		}
		return userOperationsWrapper;
	}

	private static Collection<RefactoringEvent> getRefactoringDescriptors(IPath codingspectatorRefactoringsPath, LogType type, String username,
			String workspaceID, String codingspectatorVersion) throws CoreException {
		String refactoringHistoryFolder= type == LogType.ECLIPSE ? "eclipse-refactorings" : RefactoringLog.toString(type);
		RefactoringLog refactoringLog= new RefactoringLog(codingspectatorRefactoringsPath.append(refactoringHistoryFolder));
		Collection<CapturedRefactoringDescriptor> refactoringDescriptors= refactoringLog.getRefactoringDescriptors();
		return CodingSpectatorDataExtractor.toRefactoringEvents(refactoringDescriptors, username, workspaceID, codingspectatorVersion, type);
	}

	static Collection<RefactoringEvent> toRefactoringEvents(Collection<CapturedRefactoringDescriptor> capturedRefactoringDescriptors, String username,
			String workspaceID, String codingspectatorVersion, LogType refactoringKind) {
		Collection<RefactoringEvent> refactoringEvents= new ArrayList<RefactoringEvent>();
		for (CapturedRefactoringDescriptor capturedRefactoringDescriptor : capturedRefactoringDescriptors) {
			refactoringEvents.add(new RefactoringEvent(capturedRefactoringDescriptor, username, workspaceID, codingspectatorVersion, refactoringKind));
		}
		return refactoringEvents;
	}

	private static List<EFSFile> versionsUnderStudy(EFSFile parentFolder) throws CoreException {
		List<String> fileNames= new ArrayList<String>(parentFolder.childNames());
		List<EFSFile> filteredFiles= new ArrayList<EFSFile>();
		String match= ".svn";
		fileNames.removeAll(Arrays.asList(match));
		for (String fileName : fileNames) {
			boolean hasValidVersionNumber= Pattern.compile("\\d\\.\\d\\.\\d\\.\\d*").matcher(fileName).matches();

			//FIXME: See issue #244.
//			boolean isNotTooOld= fileName.compareTo("1.0.0.201105300951") >= 0;
			boolean isNotTooOld= true;

			if (fileName.equals("1.0.0.qualifier") || (hasValidVersionNumber && isNotTooOld)) {
				filteredFiles.add(parentFolder.append(fileName));
			}
		}
		return filteredFiles;
	}

	private static List<EFSFile> childrenExceptSVNFolders(EFSFile parentFolder) throws CoreException {
		List<String> fileNames= new ArrayList<String>(parentFolder.childNames());
		List<EFSFile> filteredFiles= new ArrayList<EFSFile>();
		String match= ".svn";
		fileNames.removeAll(Arrays.asList(match));
		for (String fileName : fileNames) {
			filteredFiles.add(parentFolder.append(fileName));
		}
		return filteredFiles;
	}

}
