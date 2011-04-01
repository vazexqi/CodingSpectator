/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.files;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;

import edu.illinois.codingspectator.codingtracker.helpers.FileHelper;
import edu.illinois.codingspectator.codingtracker.operations.JavaProjectsUpkeeper;
import edu.illinois.codingspectator.codingtracker.operations.OperationLexer;
import edu.illinois.codingspectator.codingtracker.operations.OperationTextChunk;
import edu.illinois.codingspectator.codingtracker.operations.UserOperation;
import edu.illinois.codingtracker.jdt.project.manipulation.JavaProjectHelper;

/**
 * 
 * @author Stas Negara
 * 
 */
public abstract class FileOperation extends UserOperation {

	private static final String FILE_PATH_SEPARATOR= "/";

	private static final String PACKAGE_NAME_SEPARATOR= ".";

	protected String filePath;

	//All the following fields are calculated, so do not serialize/deserialize them

	protected String projectName;

	protected String sourceFolderName;

	protected String packageName;

	protected String fileName;

	public FileOperation() {
		super();
	}

	public FileOperation(IFile file) {
		super();
		filePath= FileHelper.getPortableFilePath(file);
	}

	private void initFragmentNames() {
		String[] filePathFragments= filePath.split(FILE_PATH_SEPARATOR);
		//ignore filePathFragments[0] which is an empty string, because the file path starts with '/'
		projectName= filePathFragments[1];
		sourceFolderName= filePathFragments[2];
		fileName= filePathFragments[filePathFragments.length - 1];
		if (isValidPackageName(filePathFragments)) {
			packageName= filePathFragments[3];
			for (int i= 4; i < filePathFragments.length - 1; i++) {
				packageName= packageName + PACKAGE_NAME_SEPARATOR + filePathFragments[i];
			}
		} else {
			packageName= "";
			for (int i= 3; i < filePathFragments.length - 1; i++) {
				sourceFolderName= sourceFolderName + FILE_PATH_SEPARATOR + filePathFragments[i];
			}
		}
	}

	private boolean isValidPackageName(String[] filePathFragments) {
		if (filePathFragments.length <= 4) {
			return false;
		}
		for (int i= 3; i < filePathFragments.length - 1; i++) {
			if (!Character.isJavaIdentifierStart(filePathFragments[i].charAt(0))) {
				return false;
			}
		}
		return true;
	}

	protected void createCompilationUnit(String content) throws CoreException {
		IJavaProject javaProject= JavaProjectsUpkeeper.findOrCreateJavaProject(projectName);
		IPackageFragmentRoot fragmentRoot= JavaProjectHelper.addSourceContainer(javaProject, sourceFolderName);
		IPackageFragment packageFragment= fragmentRoot.createPackageFragment(packageName, true, null);
		packageFragment.createCompilationUnit(fileName, content, true, null);
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		textChunk.append(filePath);
	}

	@Override
	protected void initializeFrom(OperationLexer operationLexer) {
		filePath= operationLexer.getNextLexeme();
		initFragmentNames();
	}

	@Override
	public String toString() {
		StringBuffer sb= new StringBuffer();
		sb.append("File path: " + filePath + "\n");
		sb.append(super.toString());
		return sb.toString();
	}

}
