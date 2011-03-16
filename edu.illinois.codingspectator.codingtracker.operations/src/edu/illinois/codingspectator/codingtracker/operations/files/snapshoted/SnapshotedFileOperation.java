/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.files.snapshoted;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;

import edu.illinois.codingspectator.codingtracker.helpers.FileHelper;
import edu.illinois.codingspectator.codingtracker.operations.OperationLexer;
import edu.illinois.codingspectator.codingtracker.operations.OperationTextChunk;
import edu.illinois.codingspectator.codingtracker.operations.files.FileOperation;
import edu.illinois.codingtracker.jdt.project.manipulation.JavaProjectHelper;

/**
 * 
 * @author Stas Negara
 * 
 */
public abstract class SnapshotedFileOperation extends FileOperation {

	private String fileContent;

	private static Set<IJavaProject> javaProjects= new HashSet<IJavaProject>();

	public SnapshotedFileOperation() {
		super();
	}

	public SnapshotedFileOperation(IFile snapshotedFile) {
		super(snapshotedFile);
		fileContent= FileHelper.getFileContent(snapshotedFile.getLocation().toFile());
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		super.populateTextChunk(textChunk);
		textChunk.append(fileContent);
	}

	@Override
	protected void initializeFrom(OperationLexer operationLexer) {
		super.initializeFrom(operationLexer);
		fileContent= operationLexer.getNextLexeme();
	}

	@Override
	public void replay() throws CoreException {
		IJavaProject javaProject= getExistingJavaProject();
		if (javaProject == null) {
			javaProject= JavaProjectHelper.createJavaProject(projectName, "bin");
			javaProjects.add(javaProject);
		}
		IPackageFragmentRoot fragmentRoot= JavaProjectHelper.addSourceContainer(javaProject, sourceFolderName);
		IPackageFragment packageFragment= fragmentRoot.createPackageFragment(packageName, true, null);
		packageFragment.createCompilationUnit(fileName, fileContent, true, null);
	}

	private IJavaProject getExistingJavaProject() {
		for (IJavaProject javaProject : javaProjects) {
			if (javaProject.getElementName().equals(projectName)) {
				if (javaProject.exists()) {
					return javaProject;
				} else {
					javaProjects.remove(javaProject);
					return null;
				}

			}
		}
		return null;
	}

	@Override
	public String toString() {
		StringBuffer sb= new StringBuffer();
		sb.append("File content: " + fileContent + "\n");
		sb.append(super.toString());
		return sb.toString();
	}

	protected void checkSnapshotMatchesTheExistingFile() {
		IResource workspaceResource= ResourcesPlugin.getWorkspace().getRoot().findMember(filePath);
		if (workspaceResource != null) {
			File existingFile= workspaceResource.getLocation().toFile();
			if (!fileContent.equals(FileHelper.getFileContent(existingFile))) {
				throw new RuntimeException("The snapshot file does not match the existing file: " + filePath);
			}
		}
	}

}
