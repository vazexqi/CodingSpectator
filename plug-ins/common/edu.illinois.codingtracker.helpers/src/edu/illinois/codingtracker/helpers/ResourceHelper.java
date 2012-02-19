/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingtracker.helpers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * 
 * @author Stas Negara
 * 
 */
public class ResourceHelper {

	private static final String JAVA_FILE_EXTENSION= "java";

	public static Charset UNIVERSAL_CHARSET= Charset.forName("UTF-8"); //should always exist, should not throw an exception here

	public static String getCharsetNameForFile(IFile file) {
		return getCharsetForFile(file).name();
	}

	private static Charset getCharsetForFile(IFile file) {
		String charsetName= null;
		try {
			charsetName= file.getCharset();
		} catch (CoreException e) {
			//actually, should not happen, but anyway, do nothing
		}
		return getCharsetForNameOrDefault(charsetName);
	}

	public static Charset getCharsetForNameOrDefault(String charsetName) {
		Charset charset= null;
		try {
			charset= Charset.forName(charsetName);
		} catch (Exception ex) {
			charset= Charset.defaultCharset();
		}
		return charset;
	}

	public static String readFileContent(IFile workspaceFile) {
		return readFileContent(getFileForResource(workspaceFile), getCharsetForFile(workspaceFile));
	}

	public static String readFileContent(IFile workspaceFile, String charsetName) {
		return readFileContent(getFileForResource(workspaceFile), getCharsetForNameOrDefault(charsetName));
	}

	/**
	 * Should be used only for reading the files produced by CodingTracker itself
	 * 
	 * @param file
	 * @return
	 */
	public static String readFileContent(File file) {
		return readFileContent(file, UNIVERSAL_CHARSET);
	}

	/**
	 * Should be used only for files that are read using UNIVERSAL_CHARSET.
	 * 
	 * @param file
	 * @param content
	 * @return
	 */
	public static boolean isReadCompletely(File file, String content) {
		return file.length() == content.getBytes(UNIVERSAL_CHARSET).length;
	}

	private static String readFileContent(File file, Charset charset) {
		String fileContent= ""; //Should return an empty string rather than null if something goes wrong
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
			fileContent= new String(bytes, charset);
		} catch (Exception e) {
			Debugger.logExceptionToErrorLog(e, Messages.Recorder_ReadFileException);
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
			bufferedWriter= new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, append), UNIVERSAL_CHARSET));
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

	public static String getPortableResourcePath(IResource resource) {
		return resource.getFullPath().toPortableString();
	}

	public static IResource findWorkspaceMember(IPath memberPath) {
		return getWorkspaceRoot().findMember(memberPath);
	}

	public static IResource findWorkspaceMember(String memberPath) {
		return getWorkspaceRoot().findMember(memberPath);
	}

	public static IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	public static boolean isFileBufferSynchronized(IFile file) {
		ITextFileBuffer textFileBuffer= getTextFileBuffer(file.getFullPath());
		return textFileBuffer != null && textFileBuffer.isSynchronized();
	}

	public static boolean isFileBufferNotSynchronized(IFile file) {
		ITextFileBuffer textFileBuffer= getTextFileBuffer(file.getFullPath());
		return textFileBuffer != null && !textFileBuffer.isSynchronized();
	}

	public static ITextFileBuffer getTextFileBuffer(IPath fullFilePath) {
		return FileBuffers.getTextFileBufferManager().getTextFileBuffer(fullFilePath, LocationKind.IFILE);
	}

	public static Map<IFile, String> getEntriesRevisions(IFile cvsEntriesFile, IPath relativePath) {
		return getEntriesRevisions(getFileForResource(cvsEntriesFile), relativePath, getCharsetForFile(cvsEntriesFile));
	}

	/**
	 * Should be used only for reading the files produced by CodingTracker itself
	 * 
	 * @param cvsEntriesFile
	 * @param relativePath
	 * @return
	 */
	public static Map<IFile, String> getEntriesRevisions(File cvsEntriesFile, IPath relativePath) {
		return getEntriesRevisions(cvsEntriesFile, relativePath, UNIVERSAL_CHARSET);
	}

	private static Map<IFile, String> getEntriesRevisions(File cvsEntriesFile, IPath relativePath, Charset charset) {
		String[] entries= readFileContent(cvsEntriesFile, charset).split("\n");
		Map<IFile, String> entriesRevisions= new HashMap<IFile, String>();
		for (String entry : entries) {
			String[] entryElements= entry.split("/");
			if (entryElements.length > 2 && entryElements[0].isEmpty() && entryElements[1].endsWith(".java")) {
				IPath entryFilePath= relativePath.append(entryElements[1]);
				IResource entryFile= findWorkspaceMember(entryFilePath);
				if (entryFile instanceof IFile && entryFile.exists()) {
					entriesRevisions.put((IFile)entryFile, entryElements[2]);
				}
			}
		}
		return entriesRevisions;
	}

	public static Set<IFile> getFilesFromRevisions(Set<FileRevision> fileRevisions) {
		Set<IFile> files= new HashSet<IFile>();
		for (FileRevision fileRevision : fileRevisions) {
			files.add(fileRevision.getFile());
		}
		return files;
	}

	public static Set<IFile> getContainedJavaFiles(IResource resource) throws CoreException {
		Set<IFile> containedJavaFiles= new HashSet<IFile>();
		if (resource instanceof IFile) {
			IFile file= (IFile)resource;
			if (isJavaFile(file)) {
				containedJavaFiles.add(file);
			}
		} else if (resource instanceof IContainer) {
			for (IResource containedResource : ((IContainer)resource).members()) {
				containedJavaFiles.addAll(getContainedJavaFiles(containedResource));
			}
		}
		return containedJavaFiles;
	}

	public static Set<String> getFilePathsPrefixedBy(String prefix, Set<String> allFilePaths) {
		Set<String> filePaths= new HashSet<String>();
		for (String filePath : allFilePaths) {
			if (filePath.equals(prefix) || filePath.startsWith(prefix + IPath.SEPARATOR)) {
				filePaths.add(filePath);
			}
		}
		return filePaths;
	}

	public static File getFileForResource(IResource resource) {
		return resource.getLocation().toFile();
	}

	public static void checkResourceExists(IResource resource, String errorMessage) {
		if (resource == null || !resource.exists()) {
			throw new RuntimeException(errorMessage);
		}
	}

	public static boolean isJavaFile(IFile file) {
		return JAVA_FILE_EXTENSION.equals(file.getFileExtension());
	}

}
