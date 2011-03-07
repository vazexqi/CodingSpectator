/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

/**
 * 
 * @author Stas Negara
 * 
 */
public class FileHelper {

	public static String getFileContent(File file) {
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

	public static String getPortableFilePath(IFile file) {
		return file.getFullPath().toPortableString();
	}

	public static IResource findWorkspaceMemeber(IPath memberPath) {
		return ResourcesPlugin.getWorkspace().getRoot().findMember(memberPath);
	}

	public static Map<IFile, String> getEntriesVersions(File cvsEntriesFile, IPath relativePath) {
		Map<IFile, String> entriesVersions= new HashMap<IFile, String>();
		String[] entries= getFileContent(cvsEntriesFile).split("\n");
		for (String entry : entries) {
			String[] entryElements= entry.split("/");
			if (entryElements.length > 2 && entryElements[0].isEmpty() && entryElements[1].endsWith(".java")) {
				IPath entryFilePath= relativePath.append(entryElements[1]);
				IResource entryFile= findWorkspaceMemeber(entryFilePath);
				if (entryFile != null) {
					entriesVersions.put((IFile)entryFile, entryElements[2]);
				}
			}
		}
		return entriesVersions;
	}

}
