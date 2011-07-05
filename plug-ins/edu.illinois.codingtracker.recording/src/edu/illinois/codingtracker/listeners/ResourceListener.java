/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.listeners;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.core.internal.resources.IResourceListener;
import org.eclipse.core.internal.resources.Resource;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.wc.admin.SVNAdminArea;
import org.tmatesoft.svn.core.internal.wc.admin.SVNAdminAreaFactory;
import org.tmatesoft.svn.core.internal.wc.admin.SVNEntry;

import edu.illinois.codingtracker.helpers.FileRevision;
import edu.illinois.codingtracker.helpers.ResourceHelper;


/**
 * 
 * @author Stas Negara
 * 
 */
@SuppressWarnings("restriction")
public class ResourceListener extends BasicListener implements IResourceListener {

	private enum Manipulation {
		ADDED, REMOVED, CHANGED
	};

	//Populated sets:

	private final Set<IFile> externallyAddedJavaFiles= new HashSet<IFile>();

	private final Set<IFile> externallyChangedJavaFiles= new HashSet<IFile>();

	private final Set<IFile> externallyRemovedJavaFiles= new HashSet<IFile>();

	private final Set<IFile> svnAddedJavaFiles= new HashSet<IFile>();

	private final Set<IFile> svnChangedJavaFiles= new HashSet<IFile>();

	//Calculated sets:

	private final Set<IFile> externallyModifiedJavaFiles= new HashSet<IFile>();

	private final Set<FileRevision> updatedJavaFileRevisions= new HashSet<FileRevision>();

	private final Set<FileRevision> svnInitiallyCommittedJavaFileRevisions= new HashSet<FileRevision>();

	private final Set<FileRevision> svnCommittedJavaFileRevisions= new HashSet<FileRevision>();

	//SVN entries caching

	private final Map<String, SVNAdminArea> svnAdminAreaCache= new HashMap<String, SVNAdminArea>();


	public static void register() {
		Resource.resourceListener= new ResourceListener();
	}

	@Override
	public void createdResource(IResource resource, int updateFlags, boolean success) {
		if (isRecordedResource(resource) && isRefactoring) {//Record only during refactorings to avoid recording huge checked out projects
			operationRecorder.recordCreatedResource(resource, updateFlags, success);
		}
	}

	@Override
	public void movedResource(IResource resource, IPath destination, int updateFlags, boolean success) {
		if (isRecordedResource(resource)) {
			operationRecorder.recordMovedResource(resource, destination, updateFlags, success);
		}
	}

	@Override
	public void copiedResource(IResource resource, IPath destination, int updateFlags, boolean success) {
		if (isRecordedResource(resource)) {
			operationRecorder.recordCopiedResource(resource, destination, updateFlags, success);
		}
	}

	@Override
	public void deletedResource(IResource resource, int updateFlags, boolean success) {
		if (isRecordedResource(resource)) {
			operationRecorder.recordDeletedResource(resource, updateFlags, success);
		}
	}

	@Override
	public void externallyModifiedResource(IResource resource, boolean isDeleted) {
		if (resource instanceof IFile) {
			Manipulation manipulation= isDeleted ? Manipulation.REMOVED : Manipulation.CHANGED;
			handleExternalFileManipulation((IFile)resource, manipulation);
		}
	}

	@Override
	public void externallyCreatedResource(IResource resource) {
		if (resource instanceof IFile) {
			handleExternalFileManipulation((IFile)resource, Manipulation.ADDED);
		}
	}

	@Override
	public void refreshedResource(IResource resource) {
		if (!isRefactoring) {
			svnAdminAreaCache.clear(); //Clear SVN entries cache before processing
			calculateSets();
			recordSets();
			updateKnownFiles();
		}
		//Always clear sets for the following refresh operation.
		clearExternallyManipulatedFileSets();
	}

	@Override
	public void savedFile(IFile file, boolean success) {
		if (ResourceHelper.isJavaFile(file)) {
			operationRecorder.recordSavedFile(file, success);
		}
	}

	@Override
	public void savedCompareEditor(Object compareEditor, boolean success) {
		operationRecorder.recordSavedCompareEditor((CompareEditor)compareEditor, success);
	}

	private boolean isRecordedResource(IResource resource) {
		if (resource instanceof IFile) {
			return ResourceHelper.isJavaFile((IFile)resource);
		}
		return true;
	}

	private void handleExternalFileManipulation(IFile file, Manipulation manipulation) {
		if (ResourceHelper.isJavaFile(file)) {
			switch (manipulation) {
				case ADDED:
					externallyAddedJavaFiles.add(file);
					break;
				case REMOVED:
					externallyRemovedJavaFiles.add(file);
					break;
				case CHANGED:
					externallyChangedJavaFiles.add(file);
					break;
			}
		} else if ("svn-base".equals(file.getFileExtension())) {
			IFile javaSourceFile= getJavaSourceFileForSVNFile(file);
			if (javaSourceFile != null) {
				//TODO: Consider REMOVED to track files that a removed as a part of an update operation
				switch (manipulation) {
					case ADDED:
						svnAddedJavaFiles.add(javaSourceFile);
						break;
					case CHANGED:
						svnChangedJavaFiles.add(javaSourceFile);
						break;
				}
			}
		}
	}

	/**
	 * Returns null if there is no corresponding Java source file (e.g. when the SVN file is not
	 * from text-base folder).
	 * 
	 * @param svnFile
	 * @return
	 */
	private IFile getJavaSourceFileForSVNFile(IFile svnFile) {
		String fileName= svnFile.getName();
		if (fileName.endsWith(".java.svn-base")) {
			IPath fileFullPath= svnFile.getFullPath();
			String parentDir= fileFullPath.segment(fileFullPath.segmentCount() - 2);
			if (parentDir.equals("text-base")) {
				String javaSourceFileName= fileName.substring(0, fileName.lastIndexOf("."));
				IPath javaSourceFilePath= fileFullPath.removeLastSegments(3).append(javaSourceFileName);
				IResource javaResource= ResourceHelper.findWorkspaceMember(javaSourceFilePath);
				if (javaResource instanceof IFile && javaResource.exists()) {
					return (IFile)javaResource;
				}
			}
		}
		return null;
	}

	private void calculateSets() {
		//should be done only in this order
		calculateSVNSets();
		calculateExternallyModifiedJavaFiles();
	}

	private void calculateSVNSets() {
		for (IFile file : svnChangedJavaFiles) {
			FileRevision fileRevision= getSVNFileRevision(file);
			if (externallyChangedJavaFiles.contains(file)) {
				updatedJavaFileRevisions.add(fileRevision); //if both the java file and its svn storage have changed, then its an update
			} else if (!externallyAddedJavaFiles.contains(file)) {
				svnCommittedJavaFileRevisions.add(fileRevision); //if only svn storage of a java file has changed, its a commit
			}
		}
		for (IFile file : svnAddedJavaFiles) {
			//if only svn storage was added for a file, its an initial commit
			if (!externallyAddedJavaFiles.contains(file) && !externallyChangedJavaFiles.contains(file)) {
				svnInitiallyCommittedJavaFileRevisions.add(getSVNFileRevision(file));
			}
		}
	}

	private void calculateExternallyModifiedJavaFiles() {
		for (IFile file : externallyChangedJavaFiles) {
			boolean isUpdated= false;
			for (FileRevision fileRevision : updatedJavaFileRevisions) {
				if (fileRevision.getFile().equals(file)) {
					isUpdated= true;
					break;
				}
			}
			if (!isUpdated) {
				externallyModifiedJavaFiles.add(file);
			}
		}
	}

	private void recordSets() {
		operationRecorder.recordExternallyModifiedFiles(externallyRemovedJavaFiles, true);
		operationRecorder.recordExternallyModifiedFiles(externallyModifiedJavaFiles, false);
		operationRecorder.recordUpdatedFiles(updatedJavaFileRevisions);
		operationRecorder.recordCommittedFiles(svnInitiallyCommittedJavaFileRevisions, true, true);
		operationRecorder.recordCommittedFiles(svnCommittedJavaFileRevisions, false, true);
	}

	private void updateKnownFiles() {
		externallyRemovedJavaFiles.addAll(ResourceHelper.getFilesFromRevisions(updatedJavaFileRevisions)); //updated files become unknown (like removed)
		externallyRemovedJavaFiles.addAll(externallyModifiedJavaFiles); //externally modified files become unknown
		knownFilesRecorder.removeKnownFiles(externallyRemovedJavaFiles);
	}

	private void clearExternallyManipulatedFileSets() {
		externallyAddedJavaFiles.clear();
		externallyChangedJavaFiles.clear();
		externallyRemovedJavaFiles.clear();
		svnAddedJavaFiles.clear();
		svnChangedJavaFiles.clear();
		externallyModifiedJavaFiles.clear();
		updatedJavaFileRevisions.clear();
		svnInitiallyCommittedJavaFileRevisions.clear();
		svnCommittedJavaFileRevisions.clear();
	}

	private FileRevision getSVNFileRevision(IFile file) {
		FileRevision fileRevision= new FileRevision(file, "0", "0"); //default file revision
		try {
			IContainer parent= file.getParent();
			String parentPath= ResourceHelper.getPortableResourcePath(parent);
			SVNAdminArea svnAdminArea= svnAdminAreaCache.get(parentPath);
			if (svnAdminArea == null) {
				svnAdminArea= SVNAdminAreaFactory.open(ResourceHelper.getFileForResource(parent), Level.OFF);
			}
			if (svnAdminArea != null) {
				svnAdminAreaCache.put(parentPath, svnAdminArea);
				SVNEntry svnEntry= svnAdminArea.getEntry(file.getName(), true);
				if (svnEntry != null) {
					fileRevision= new FileRevision(file, String.valueOf(svnEntry.getRevision()), String.valueOf(svnEntry.getCommittedRevision()));
				}
			}
		} catch (SVNException e) {
			//ignore SVN exceptions
		} catch (Exception e) {
			//ignore all other exceptions as well
		}
		return fileRevision;
	}

}
