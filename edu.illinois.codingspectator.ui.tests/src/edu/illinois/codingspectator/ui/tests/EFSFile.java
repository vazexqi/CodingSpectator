/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.ui.tests;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * 
 * @author Mohsen Vakilian
 * 
 */
public class EFSFile {

	private IPath path;

	public EFSFile(IPath path) {
		this.path= path;
	}

	public EFSFile(String path) {
		this.path= Path.fromOSString(path);
	}

	private IFileStore getFileStore() {
		return EFS.getLocalFileSystem().getStore(path);
	}

	public boolean exists() {
		return getFileStore().fetchInfo().exists();
	}

	public void delete() throws CoreException {
		getFileStore().delete(EFS.NONE, null);
	}

	public List<String> childNames() throws CoreException {
		return Arrays.asList(getFileStore().childNames(EFS.NONE, null));
	}

	public EFSFile append(String pathElement) {
		return new EFSFile(path.append(pathElement));
	}

}
