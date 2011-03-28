/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.helpers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import edu.illinois.codingtracker.jdt.project.manipulation.JavaProjectHelper;

/**
 * 
 * @author Stas Negara
 * 
 */
public class FileHelper {

	public static String readFileContent(IResource resource) {
		return readFileContent(getFileForResource(resource));
	}

	public static String readFileContent(File file) {
		String fileContent= null;
		InputStream inputStream= null;
		try {
			inputStream= new FileInputStream(file);
			int fileLength= (int)file.length(); //should not exceed 2Gb
			byte[] bytes= new byte[fileLength];
			int offset= 0;
			int readBytes= 0;
			while (offset < fileLength && readBytes >= 0) {
				readBytes= inputStream.read(bytes, offset, fileLength - offset);
				offset+= readBytes;
			}
			if (offset < fileLength) {
				throw new RuntimeException(Messages.Recorder_CompleteReadUnknownFileException);
			}
			fileContent= new String(bytes);
		} catch (Exception e) {
			Debugger.logExceptionToErrorLog(e, Messages.Recorder_ReadUnknownFileException);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					//do nothing
				}
			}
		}
		return fileContent;
	}

	public static void writeFileContent(File file, CharSequence text, boolean append) throws IOException {
		BufferedWriter bufferedWriter= null;
		try {
			bufferedWriter= new BufferedWriter(new FileWriter(file, append));
			bufferedWriter.append(text);
			bufferedWriter.flush();
		} finally {
			if (bufferedWriter != null) {
				try {
					bufferedWriter.close();
				} catch (IOException e) {
					//do nothing
				}
			}
		}
	}

	public static void ensureFileExists(File file) throws IOException {
		if (!file.exists()) {
			file.getParentFile().mkdirs();
			file.createNewFile();
		}
	}

	public static String getPortableFilePath(IFile file) {
		return file.getFullPath().toPortableString();
	}

	public static IResource findWorkspaceMember(IPath memberPath) {
		return ResourcesPlugin.getWorkspace().getRoot().findMember(memberPath);
	}

	public static boolean isFileBufferSynchronized(IFile file) {
		IFileBuffer fileBuffer= getFileBuffer(file.getFullPath());
		return fileBuffer != null && fileBuffer.isSynchronized();
	}

	public static IFileBuffer getFileBuffer(IPath fullFilePath) {
		return FileBuffers.getTextFileBufferManager().getFileBuffer(fullFilePath, LocationKind.IFILE);
	}

	/**
	 * Should be called from an UI thread
	 * 
	 * @return
	 */
	public static void clearWorkspace() {
		getActivePage().closeAllEditors(false);
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			try {
				JavaProjectHelper.delete(project);
			} catch (CoreException e) {
				throw new RuntimeException("Could not delete project \"" + project.getName() + "\"", e);
			}
		}
	}

	/**
	 * Should be called from an UI thread
	 * 
	 * @return
	 */
	public static IWorkbenchPage getActivePage() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	}

	public static Map<IFile, String> getEntriesVersions(IFile cvsEntriesFile, IPath relativePath) {
		return getEntriesVersions(getFileForResource(cvsEntriesFile), relativePath);
	}

	public static Map<IFile, String> getEntriesVersions(File cvsEntriesFile, IPath relativePath) {
		String[] entries= readFileContent(cvsEntriesFile).split("\n");
		Map<IFile, String> entriesVersions= new HashMap<IFile, String>();
		for (String entry : entries) {
			String[] entryElements= entry.split("/");
			if (entryElements.length > 2 && entryElements[0].isEmpty() && entryElements[1].endsWith(".java")) {
				IPath entryFilePath= relativePath.append(entryElements[1]);
				IResource entryFile= findWorkspaceMember(entryFilePath);
				if (entryFile != null) {
					entriesVersions.put((IFile)entryFile, entryElements[2]);
				}
			}
		}
		return entriesVersions;
	}

	public static File getFileForResource(IResource resource) {
		return resource.getLocation().toFile();
	}

}
