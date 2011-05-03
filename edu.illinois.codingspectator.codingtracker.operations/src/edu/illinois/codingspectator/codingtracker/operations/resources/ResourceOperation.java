/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.operations.resources;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IParent;

import edu.illinois.codingspectator.codingtracker.helpers.ResourceHelper;
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
public abstract class ResourceOperation extends UserOperation {

	private static final String FILE_PATH_SEPARATOR= String.valueOf(IPath.SEPARATOR);

	private static final String PACKAGE_NAME_SEPARATOR= ".";

	protected String resourcePath;


	public ResourceOperation() {
		super();
	}

	public ResourceOperation(IResource resource) {
		super();
		resourcePath= ResourceHelper.getPortableResourcePath(resource);
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		textChunk.append(resourcePath);
	}

	@Override
	protected void initializeFrom(OperationLexer operationLexer) {
		resourcePath= operationLexer.getNextLexeme();
	}

	protected void createCompilationUnit(String content) throws CoreException {
		IPackageFragment packageFragment= (IPackageFragment)findOrCreateParent(resourcePath);// for compilation units parent is always package fragment		
		String[] filePathFragments= resourcePath.split(FILE_PATH_SEPARATOR);
		String fileName= filePathFragments[filePathFragments.length - 1];
		packageFragment.createCompilationUnit(fileName, content, true, null);
	}

	protected IParent findOrCreateParent(String path) throws CoreException {
		String[] filePathFragments= path.split(FILE_PATH_SEPARATOR);
		//ignore filePathFragments[0] which is an empty string, because the file path starts with '/'
		if (filePathFragments.length <= 2) {
			return null; //no parent
		}
		String projectName= filePathFragments[1];
		IJavaProject javaProject= JavaProjectsUpkeeper.findOrCreateJavaProject(projectName);
		if (filePathFragments.length <= 3) {
			return javaProject;
		}
		String sourceFolderName= filePathFragments[2];
		String packageName= "";
		if (hasValidPackageName(filePathFragments)) {
			packageName= filePathFragments[3];
			for (int i= 4; i < filePathFragments.length - 1; i++) {
				packageName= packageName + PACKAGE_NAME_SEPARATOR + filePathFragments[i];
			}
		} else {
			for (int i= 3; i < filePathFragments.length - 1; i++) {
				sourceFolderName= sourceFolderName + FILE_PATH_SEPARATOR + filePathFragments[i];
			}
		}
		IPackageFragmentRoot fragmentRoot= JavaProjectHelper.addSourceContainer(javaProject, sourceFolderName);
		IPackageFragment packageFragment= fragmentRoot.createPackageFragment(packageName, true, null);
		return packageFragment;
	}

	private boolean hasValidPackageName(String[] filePathFragments) {
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

	@Override
	public String toString() {
		StringBuffer sb= new StringBuffer();
		sb.append("Resource path: " + resourcePath + "\n");
		sb.append(super.toString());
		return sb.toString();
	}

}
