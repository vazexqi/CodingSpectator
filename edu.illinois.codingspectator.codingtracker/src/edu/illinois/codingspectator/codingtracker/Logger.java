/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.history.RefactoringExecutionEvent;
import org.eclipse.ltk.internal.core.refactoring.history.DefaultRefactoringDescriptor;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringContributionManager;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryService;

/**
 * 
 * @author Stas Negara
 * 
 * 
 */
@SuppressWarnings("restriction")
public class Logger {

	private BufferedWriter logFileWriter= null;

	private File knownFilesFile= null;

	private final Properties knownfiles= new Properties(); //Is thread-safe since SE 6

	public static final IPath watchedDirectory= Platform.getStateLocation(
			Platform.getBundle(Messages.Logger_LTKBundleName));

	private static final String featureVersion= RefactoringHistoryService.getFeatureVersion().toString();

	private static final String LOGFILE_NAME= Messages.Logger_CodeChangesFileName;

	private static final String KNOWNFILES_FILE_NAME= Messages.Logger_KnownFilesFileName;

	private IFile lastEditedFile= null;

	private static final long REFRESH_INTERVAL= 7 * 24 * 60 * 60 * 1000; //Refresh knownfiles every 7 days

	//Used symbols: 16, remaining symbols:
	//g i j k n q v w y z
	
	private static final String ECLIPSE_SESSION_SYMBOL= "l"; //$NON-NLS-1$

	private static final String FILE_CLOSED_SYMBOL= "c"; //$NON-NLS-1$

	private static final String FILE_SAVED_SYMBOL= "s"; //$NON-NLS-1$

	//Modification outside of Eclipse, or it may be a move/copy refactoring that overwrites a file displayed in a viewer
	private static final String FILE_EXTERNALLY_MODIFIED_SYMBOL= "x"; //$NON-NLS-1$ 

	private static final String FILE_UPDATED_SYMBOL= "m"; //$NON-NLS-1$

	private static final String FILE_COMMITTED_SYMBOL= "o"; //$NON-NLS-1$

	private static final String FILE_REFACTORED_SAVED_SYMBOL= "a"; //$NON-NLS-1$

	private static final String FILE_NEW_SYMBOL= "f"; //$NON-NLS-1$

	private static final String FILE_EDIT_SYMBOL= "e"; //$NON-NLS-1$

	private static final String TEXT_CHANGE_PERFORMED_SYMBOL= "t"; //$NON-NLS-1$

	private static final String TEXT_CHANGE_UNDONE_SYMBOL= "h"; //$NON-NLS-1$

	private static final String TEXT_CHANGE_REDONE_SYMBOL= "d"; //$NON-NLS-1$

	private static final String REFACTORING_STARTED_SYMBOL= "b"; //$NON-NLS-1$

	private static final String REFACTORING_PERFORMED_SYMBOL= "p"; //$NON-NLS-1$

	private static final String REFACTORING_UNDONE_SYMBOL= "u"; //$NON-NLS-1$

	private static final String REFACTORING_REDONE_SYMBOL= "r"; //$NON-NLS-1$


	public Logger() {
		IPath featureVersionPath= watchedDirectory.append(featureVersion);
		IPath logFilePath= featureVersionPath.append(LOGFILE_NAME);
		IPath knownfilesFilePath= featureVersionPath.append(KNOWNFILES_FILE_NAME);
		File logFile= new File(logFilePath.toOSString());
		knownFilesFile= new File(knownfilesFilePath.toOSString());
		logFile.getParentFile().mkdirs(); //creates directories for knownFilesFile as well
		try {
			logFile.createNewFile();
			logFileWriter= new BufferedWriter(new FileWriter(logFile, true));
		} catch (IOException e) {
			logExceptionToErrorLog(e, Messages.Logger_CreateLogFileException);
		}
		try {
			if (knownFilesFile.exists()) {
				knownfiles.load(new FileInputStream(knownFilesFile));
				refreshKnownfiles();
			} else {
				knownFilesFile.createNewFile();
			}
		} catch (Exception e) {
			logExceptionToErrorLog(e, Messages.Logger_OpenKnowfilesFileException);
		}
		TextChunk textChunk = new TextChunk(ECLIPSE_SESSION_SYMBOL);
		textChunk.append(System.currentTimeMillis());
		log(textChunk);
	}

	public void logTextEvent(TextEvent event, IFile editedFile, boolean isUndoing, boolean isRedoing) {
		//Use DocumentEvent to get correct, file-based offsets (which do not depend on expanding/collapsing of import statements,methods,etc.)
		DocumentEvent documentEvent= event.getDocumentEvent(); //should never be null in this method
//		System.out.println("Replaced text:\"" + (event.getReplacedText() == null ? "" : event.getReplacedText()) + //$NON-NLS-1$ //$NON-NLS-2$
//				"\", new text:\"" + documentEvent.getText() + "\", offset=" + documentEvent.getOffset() + ", length=" + documentEvent.getLength()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (!editedFile.equals(lastEditedFile) || !knownfiles.containsKey(editedFile.getFullPath().toPortableString())) {
			lastEditedFile= editedFile;
			ensureIsKnownFile(lastEditedFile);
			logEditedFile();
		}
		TextChunk textChunk= null;
		if (isUndoing) {
			textChunk= new TextChunk(TEXT_CHANGE_UNDONE_SYMBOL);
		} else if (isRedoing) {
			textChunk= new TextChunk(TEXT_CHANGE_REDONE_SYMBOL);
		} else {
			textChunk= new TextChunk(TEXT_CHANGE_PERFORMED_SYMBOL);
		}
		//TODO: Logging the replaced text is redundant, is it really needed? 
		//E.g. it could be used during the replay phase to check that the replaced text corresponds to the file text at this offset. 
		String replacedText= event.getReplacedText() == null ? "" : event.getReplacedText(); //$NON-NLS-1$
		textChunk.append(replacedText);
		textChunk.append(documentEvent.getText());
		textChunk.append(documentEvent.getOffset());
		textChunk.append(documentEvent.getLength());
		textChunk.append(System.currentTimeMillis());
		System.out.println("Change: " + textChunk); //$NON-NLS-1$
		log(textChunk);
	}

	private void logEditedFile() {
		TextChunk textChunk= new TextChunk(FILE_EDIT_SYMBOL);
		textChunk.append(lastEditedFile.getFullPath().toPortableString());
		textChunk.append(System.currentTimeMillis());
		System.out.println("File edited: " + textChunk); //$NON-NLS-1$
		log(textChunk);
	}

	public void logSavedFiles(Set<IFile> savedFiles, boolean isRefactoring) {
		for (IFile file : savedFiles) {
			TextChunk textChunk;
			if (isRefactoring) {
				textChunk= new TextChunk(FILE_REFACTORED_SAVED_SYMBOL);
			} else {
				textChunk= new TextChunk(FILE_SAVED_SYMBOL);
			}
			textChunk.append(file.getFullPath().toPortableString());
			textChunk.append(System.currentTimeMillis());
			System.out.println("File saved: " + textChunk); //$NON-NLS-1$
			log(textChunk);
		}
	}

	public void logExternallyModifiedFiles(Set<IFile> externallyModifiedFiles) {
		for (IFile file : externallyModifiedFiles) {
			TextChunk textChunk= new TextChunk(FILE_EXTERNALLY_MODIFIED_SYMBOL);
			textChunk.append(file.getFullPath().toPortableString());
			textChunk.append(System.currentTimeMillis());
			System.out.println("File externally modified: " + textChunk); //$NON-NLS-1$
			log(textChunk);
		}
	}

	public void logUpdatedFiles(Set<IFile> updatedFiles) {
		for (IFile file : updatedFiles) {
			TextChunk textChunk= new TextChunk(FILE_UPDATED_SYMBOL);
			textChunk.append(file.getFullPath().toPortableString());
			textChunk.append(System.currentTimeMillis());
			System.out.println("File updated: " + textChunk); //$NON-NLS-1$
			log(textChunk);
		}
	}

	public void logCommittedFiles(Set<IFile> committedFiles) {
		for (IFile file : committedFiles) {
			TextChunk textChunk= new TextChunk(FILE_COMMITTED_SYMBOL);
			textChunk.append(file.getFullPath().toPortableString());
			textChunk.append(System.currentTimeMillis());
			System.out.println("File committed: " + textChunk); //$NON-NLS-1$
			log(textChunk);
		}
	}

	public void logClosedFile(IFile file) {
		TextChunk textChunk= new TextChunk(FILE_CLOSED_SYMBOL);
		textChunk.append(file.getFullPath().toPortableString());
		textChunk.append(System.currentTimeMillis());
		System.out.println("File closed: " + textChunk); //$NON-NLS-1$
		log(textChunk);
	}
	
	public void logRefactoringStarted(){
		TextChunk textChunk= new TextChunk(REFACTORING_STARTED_SYMBOL);
		textChunk.append(System.currentTimeMillis());
		System.out.println("Refactoring started: " + textChunk);
		log(textChunk);
	}

	@SuppressWarnings("rawtypes")
	public void logRefactoringExecutionEvent(RefactoringExecutionEvent event) {
		RefactoringDescriptorProxy refactoringDescriptorProxy= event.getDescriptor();
		RefactoringDescriptor refactoringDescriptor= refactoringDescriptorProxy.requestDescriptor(new NullProgressMonitor());
//		System.out.println("Refactoring descriptor id: " + refactoringDescriptor.getID()); //$NON-NLS-1$
//		System.out.println("Project: " + refactoringDescriptor.getProject()); //$NON-NLS-1$
//		System.out.println("Flags: " + refactoringDescriptor.getFlags()); //$NON-NLS-1$
//		System.out.println("Timestamp: " + refactoringDescriptor.getTimeStamp()); //$NON-NLS-1$
		Map arguments= getRefactoringArguments(refactoringDescriptor);
		Set keys= arguments.keySet();
//		for (Object key : keys) {
//			Object value= arguments.get(key);
//			System.out.println("Argument \"" + key + "\" = \"" + value + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
//		}
		TextChunk textChunk= null;
		switch (event.getEventType()) {
			case RefactoringExecutionEvent.PERFORMED:
				textChunk= new TextChunk(REFACTORING_PERFORMED_SYMBOL);
				break;
			case RefactoringExecutionEvent.REDONE:
				textChunk= new TextChunk(REFACTORING_REDONE_SYMBOL);
				break;
			case RefactoringExecutionEvent.UNDONE:
				textChunk= new TextChunk(REFACTORING_UNDONE_SYMBOL);
				break;
			default:
				Exception e= new RuntimeException();
				logExceptionToErrorLog(e, Messages.Logger_UnrecognizedRefactoringType + event.getEventType());
		}
		textChunk.append(refactoringDescriptor.getID());
		textChunk.append(refactoringDescriptor.getProject());
		textChunk.append(refactoringDescriptor.getFlags());
		textChunk.append(keys.size());
		for (Object key : keys) {
			Object value= arguments.get(key);
			textChunk.append(key);
			textChunk.append(value);
		}
		textChunk.append(refactoringDescriptor.getTimeStamp());
		System.out.println("Refactoring info: " + textChunk); //$NON-NLS-1$
		log(textChunk);
	}

	@SuppressWarnings("rawtypes")
	private Map getRefactoringArguments(RefactoringDescriptor refactoringDescriptor) {
		Map arguments= null;
		RefactoringContribution refactoringContribution=
				RefactoringContributionManager.getInstance().getRefactoringContribution(refactoringDescriptor.getID());
		if (refactoringContribution != null)
			arguments= refactoringContribution.retrieveArgumentMap(refactoringDescriptor);
		else if (refactoringDescriptor instanceof DefaultRefactoringDescriptor)
			arguments= ((DefaultRefactoringDescriptor)refactoringDescriptor).getArguments();
		if (arguments == null) {
			Exception e= new RuntimeException();
			logExceptionToErrorLog(e, Messages.Logger_FailedToGetRefactoringArguments +
										refactoringDescriptor.getID());
		}
		return arguments;
	}

	public void removeKnownFiles(Set<IFile> files) {
		boolean hasChanged= false;
		for (IFile file : files) {
			Object removed= knownfiles.remove(file.getFullPath().toPortableString());
			if (removed != null) {
				hasChanged= true;
			}
		}
		if (hasChanged) {
			logKnownfiles();
		}
	}

	public void ensureIsKnownFile(IFile file) {
		String fileString= file.getFullPath().toPortableString();
		String timestamp= knownfiles.getProperty(fileString);
		if (timestamp == null) { //not found
			knownfiles.setProperty(fileString, String.valueOf(System.currentTimeMillis()));
			logKnownfiles();
			//save the content of a previously unknown file
			File javaFile= new File(file.getLocation().toOSString());
			if (javaFile.exists()) { //Actually, should always exist here
				TextChunk textChunk= new TextChunk(FILE_NEW_SYMBOL);
				textChunk.append(fileString);
				textChunk.append(getUnknownFileContent(javaFile));
				textChunk.append(System.currentTimeMillis());
				System.out.println("New file: " + textChunk); //$NON-NLS-1$
				log(textChunk);
			}
		}
	}

	private String getUnknownFileContent(File file) {
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
				throw new RuntimeException(Messages.Logger_CompleteReadUnknownFileException);
			}
			fileContent= new String(bytes);
		} catch (Exception e) {
			logExceptionToErrorLog(e, Messages.Logger_ReadUnknownFileException);
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

	private synchronized void log(CharSequence text) {
		try {
			logFileWriter.append(text);
			logFileWriter.flush();
		} catch (IOException e) {
			logExceptionToErrorLog(e, Messages.Logger_AppendLogFileException);
		}
	}

	private synchronized void logKnownfiles() {
		System.out.println("logKnownfiles");
		BufferedWriter knownfilesFileWriter= null;
		try {
			knownfilesFileWriter= new BufferedWriter(new FileWriter(knownFilesFile));
			knownfiles.store(knownfilesFileWriter, null);
		} catch (IOException e) {
			logExceptionToErrorLog(e, Messages.Logger_WriteKnownfilesFileException);
		} finally {
			if (knownfilesFileWriter != null) {
				try {
					knownfilesFileWriter.close();
				} catch (IOException e) {
					//do nothing
				}
			}
		}
	}

	private void refreshKnownfiles() {
		long currentTime= System.currentTimeMillis();
		Iterator<Object> keysIterator= knownfiles.keySet().iterator();
		boolean hasChanged= false;
		while (keysIterator.hasNext()) {
			Object key= keysIterator.next();
			String timestamp= knownfiles.getProperty(key.toString());
			if (currentTime - Long.valueOf(timestamp) > REFRESH_INTERVAL) {
				keysIterator.remove();
				hasChanged= true;
			}
		}
		if (hasChanged) {
			logKnownfiles();
		}
	}

	public static void logExceptionToErrorLog(Exception e, String message) {
		IStatus status= new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, message, e);
		ResourcesPlugin.getPlugin().getLog().log(status);
	}

}
