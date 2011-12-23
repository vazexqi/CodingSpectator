/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.efs;

import java.util.ArrayList;
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

	public IPath getPath() {
		return path;
	}

	public IFileStore getFileStore() {
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

	public List<EFSFile> children() throws CoreException {
		List<String> childNames= childNames();
		List<EFSFile> childEFSFiles= new ArrayList<EFSFile>();
		for (String childName : childNames) {
			childEFSFiles.add(new EFSFile(getPath().append(childName)));
		}
		return childEFSFiles;
	}

	public EFSFile append(String pathElement) {
		return new EFSFile(path.append(pathElement));
	}

	public void copyTo(EFSFile destination) throws CoreException {
		getFileStore().copy(destination.getFileStore(), EFS.OVERWRITE, null);
	}

	public void moveTo(EFSFile destination) throws CoreException {
		getFileStore().move(destination.getFileStore(), EFS.OVERWRITE, null);
	}

	public void mkdir() throws CoreException {
		getFileStore().mkdir(EFS.NONE, null);
	}

}
