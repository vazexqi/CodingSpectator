/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.operations.resources;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.ITextEditor;

import edu.illinois.codingtracker.compare.helpers.EditorHelper;
import edu.illinois.codingtracker.helpers.ResourceHelper;
import edu.illinois.codingtracker.jdt.project.manipulation.JavaProjectHelper;
import edu.illinois.codingtracker.operations.JavaProjectsUpkeeper;
import edu.illinois.codingtracker.operations.OperationLexer;
import edu.illinois.codingtracker.operations.OperationTextChunk;
import edu.illinois.codingtracker.operations.UserOperation;

/**
 * 
 * @author Stas Negara
 * 
 */
public abstract class ResourceOperation extends UserOperation {

	private static final String FILE_PATH_SEPARATOR= String.valueOf(IPath.SEPARATOR);

	private static final String PACKAGE_NAME_SEPARATOR= ".";

	//The following field is not serialized/deserialized. It is used for replay only.
	private static final Set<String> externallyModifiedResources= new HashSet<String>();

	protected String resourcePath;


	public ResourceOperation() {
		super();
	}

	public ResourceOperation(IResource resource) {
		this(resource, System.currentTimeMillis());
	}

	public ResourceOperation(IResource resource, long timestamp) {
		super(timestamp);
		resourcePath= ResourceHelper.getPortableResourcePath(resource);
	}

	public ResourceOperation(String resourcePath, long timestamp) {
		super(timestamp);
		this.resourcePath= resourcePath;
	}

	public String getResourcePath() {
		return resourcePath;
	}

	@Override
	protected void populateTextChunk(OperationTextChunk textChunk) {
		textChunk.append(resourcePath);
	}

	@Override
	protected void initializeFrom(OperationLexer operationLexer) {
		resourcePath= operationLexer.readString();
	}

	protected void createContainer() throws CoreException {
		//Simulate one more element to create the whole path as the parent of this added fake element
		findOrCreateParent(resourcePath + FILE_PATH_SEPARATOR + "fake");
	}

	public void createCompilationUnit(String content) throws CoreException {
		IPackageFragment packageFragment= findOrCreateCompilationUnitParent(resourcePath);
		String[] filePathFragments= resourcePath.split(FILE_PATH_SEPARATOR);
		String fileName= filePathFragments[filePathFragments.length - 1];
		try {
			packageFragment.createCompilationUnit(fileName, content, true, null);
		} catch (JavaModelException exception) {
			handleCompilationUnitCreationException(exception, fileName, content);
		}
		//Save the created compilation unit in case it is opened in an editor (e.g. an editor showing an externally changed file).
		saveResourceInEditor();
	}

	private void handleCompilationUnitCreationException(JavaModelException exception, String fileName, String content) throws CoreException {
		if (exception.getMessage().startsWith("Invalid name specified:")) {
			//If the name is invalid as a CompilationUnit name, create it as a regular file.
			IWorkspaceRoot workspaceRoot= ResourceHelper.getWorkspaceRoot();
			IPath fullPath= workspaceRoot.getLocation().append(resourcePath);
			try {
				ResourceHelper.writeFileContent(fullPath.toFile(), content, false);
			} catch (IOException e) {
				throw new RuntimeException("Could not create a file for a CompilationUnit with an invalid name: " + fileName, e);
			}
			workspaceRoot.refreshLocal(IResource.DEPTH_INFINITE, null);
		} else {
			throw exception; //If can not handle, just re-throw the exception.
		}
	}

	protected ITextEditor saveResourceInEditor() throws PartInitException {
		ITextEditor editor= EditorHelper.getExistingEditor(resourcePath);
		if (editor != null) {
			editor.doSave(null);
			//FIXME: Instead of sleeping, should listen to IProgressMonitor.done()
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				//do nothing
			}
		}
		return editor;
	}

	protected IPackageFragment findOrCreateCompilationUnitParent(String path) throws CoreException {
		IParent parent= findOrCreateParent(path); //Can be either null, IJavaProject, or IPackageFragment.
		if (parent instanceof IJavaProject) {
			return createPackageFragment((IJavaProject)parent, "", "");
		} else {
			return (IPackageFragment)parent;
		}
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
		return createPackageFragment(javaProject, sourceFolderName, packageName);
	}

	private IPackageFragment createPackageFragment(IJavaProject javaProject, String sourceFolderName, String packageName) throws CoreException {
		IPackageFragment packageFragment= null;
		try {
			IPackageFragmentRoot fragmentRoot= JavaProjectHelper.addSourceContainer(javaProject, sourceFolderName);
			packageFragment= fragmentRoot.createPackageFragment(packageName, true, null);
		} catch (Exception e) {
			//If the package fragment could not be created normally, clear the project's class path and try again. This helps,
			//when compilation units are created directly in a project (i.e. no source folder, no package name).
			javaProject.setRawClasspath(new IClasspathEntry[0], null);
			IPackageFragmentRoot fragmentRoot= JavaProjectHelper.addSourceContainer(javaProject, sourceFolderName);
			packageFragment= fragmentRoot.createPackageFragment(packageName, true, null);
		}
		return packageFragment;
	}

	private boolean hasValidPackageName(String[] filePathFragments) {
		if (filePathFragments.length <= 4) {
			return false;
		}
		for (int i= 3; i < filePathFragments.length - 1; i++) {
			if (!Character.isJavaIdentifierStart(filePathFragments[i].charAt(0)) || filePathFragments[i].contains("-") ||
					filePathFragments[i].contains(" ")) {
				return false;
			}
		}
		return true;
	}

	protected IResource findResource() {
		IResource resource= ResourceHelper.findWorkspaceMember(resourcePath);
		if (resource != null && !isIgnored(resource)) {
			return resource;
		}
		return null;
	}

	private boolean isIgnored(IResource resource) {
		return resource instanceof IFile && !ResourceHelper.isJavaFile((IFile)resource);
	}

	public static boolean isExternallyModifiedResource(String resourcePath) {
		return externallyModifiedResources.contains(resourcePath);
	}

	public static void addExternallyModifiedResource(String resourcePath) {
		externallyModifiedResources.add(resourcePath);
	}

	public static void removeExternallyModifiedResource(String resourcePath) {
		externallyModifiedResources.remove(resourcePath);
	}

	@Override
	public String toString() {
		StringBuffer sb= new StringBuffer();
		sb.append("Resource path: " + resourcePath + "\n");
		sb.append(super.toString());
		return sb.toString();
	}

}
