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

	private static Logger loggerInstance;

	private File currentLogFile= null;

	private File mainLogFile= null;

	private File knownFilesFile= null;

	private final Properties knownfiles= new Properties(); //Is thread-safe since SE 6

	public static final IPath WATCHED_DIRECTORY= Platform.getStateLocation(
			Platform.getBundle(Messages.Logger_LTKBundleName));

	public static final IPath CODINGTRACKER_DIRECTORY= Platform.getStateLocation(
			Platform.getBundle(Activator.PLUGIN_ID));

	private static final String FEATURE_VERSION= RefactoringHistoryService.getFeatureVersion().toString();

	private static final String LOGGER_FOLDER= Messages.Logger_ConfigurationFolder;

	private static final IPath LOGGER_PATH= WATCHED_DIRECTORY.append(FEATURE_VERSION).append(LOGGER_FOLDER);

	private static final IPath KNOWNFILES_PATH= CODINGTRACKER_DIRECTORY.append(FEATURE_VERSION);

	private static final String LOGFILE_NAME= Messages.Logger_CodeChangesFileName;

	private static final String KNOWNFILES_FILE_NAME= Messages.Logger_KnownFilesFileName;

	private IFile lastEditedFile= null;

	private static final long REFRESH_INTERVAL= 7 * 24 * 60 * 60 * 1000; //Refresh knownfiles every 7 days

	//Used symbols: 23, remaining symbols:
	//v w y

	private static final String CONFLICT_EDITOR_OPENED_SYMBOL= "g"; //$NON-NLS-1$

	private static final String ECLIPSE_SESSION_SYMBOL= "l"; //$NON-NLS-1$

	private static final String FILE_CLOSED_SYMBOL= "c"; //$NON-NLS-1$

	private static final String CONFLICT_EDITOR_CLOSED_SYMBOL= "q"; //$NON-NLS-1$

	private static final String FILE_SAVED_SYMBOL= "s"; //$NON-NLS-1$

	private static final String CONFLICT_EDITOR_SAVED_SYMBOL= "z"; //$NON-NLS-1$

	//Modification outside of Eclipse, or it may be a move/copy refactoring that overwrites a file displayed in a viewer, 
	//or SVN performs Revert operation, or some dirty file is changed externally and then saved (without refreshing)
	private static final String FILE_EXTERNALLY_MODIFIED_SYMBOL= "x"; //$NON-NLS-1$ 

	private static final String FILE_UPDATED_SYMBOL= "m"; //$NON-NLS-1$

	private static final String FILE_INITIALLY_COMMITTED_SYMBOL= "i"; //$NON-NLS-1$

	private static final String FILE_COMMITTED_SYMBOL= "o"; //$NON-NLS-1$

	private static final String FILE_REFACTORED_SAVED_SYMBOL= "a"; //$NON-NLS-1$

	private static final String FILE_NEW_SYMBOL= "f"; //$NON-NLS-1$

	private static final String FILE_EDIT_SYMBOL= "e"; //$NON-NLS-1$

	private static final String TEXT_CHANGE_PERFORMED_SYMBOL= "t"; //$NON-NLS-1$

	private static final String TEXT_CHANGE_UNDONE_SYMBOL= "h"; //$NON-NLS-1$

	private static final String TEXT_CHANGE_REDONE_SYMBOL= "d"; //$NON-NLS-1$

	private static final String CONFLICT_EDITOR_TEXT_CHANGE_PERFORMED_SYMBOL= "j"; //$NON-NLS-1$

	private static final String CONFLICT_EDITOR_TEXT_CHANGE_UNDONE_SYMBOL= "k"; //$NON-NLS-1$

	private static final String CONFLICT_EDITOR_TEXT_CHANGE_REDONE_SYMBOL= "n"; //$NON-NLS-1$

	private static final String REFACTORING_STARTED_SYMBOL= "b"; //$NON-NLS-1$

	private static final String REFACTORING_PERFORMED_SYMBOL= "p"; //$NON-NLS-1$

	private static final String REFACTORING_UNDONE_SYMBOL= "u"; //$NON-NLS-1$

	private static final String REFACTORING_REDONE_SYMBOL= "r"; //$NON-NLS-1$


	public static Logger getInstance() {
		if (loggerInstance == null) {
			loggerInstance= new Logger();
		}
		return loggerInstance;
	}

	private Logger() {
		IPath mainLogFilePath= LOGGER_PATH.append(LOGFILE_NAME);
		IPath knownfilesFilePath= KNOWNFILES_PATH.append(KNOWNFILES_FILE_NAME);
		mainLogFile= new File(mainLogFilePath.toOSString());
		knownFilesFile= new File(knownfilesFilePath.toOSString());
		mainLogFile.getParentFile().mkdirs();
		knownFilesFile.getParentFile().mkdirs();
		try {
			mainLogFile.createNewFile();
			currentLogFile= mainLogFile;
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
		TextChunk textChunk= new TextChunk(ECLIPSE_SESSION_SYMBOL);
		textChunk.append(System.currentTimeMillis());
		log(textChunk);
	}

	public void logTextEvent(TextEvent event, IFile editedFile, boolean isUndoing, boolean isRedoing) {
		//Use DocumentEvent to get correct, file-based offsets (which do not depend on expanding/collapsing of import statements,methods,etc.)
		DocumentEvent documentEvent= event.getDocumentEvent(); //should never be null in this method
		if (Activator.isInDebugMode) {
//			System.out.println("Replaced text:\"" + (event.getReplacedText() == null ? "" : event.getReplacedText()) + //$NON-NLS-1$ //$NON-NLS-2$
//					"\", new text:\"" + documentEvent.getText() + "\", offset=" + documentEvent.getOffset() + ", length=" + documentEvent.getLength()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		if (!editedFile.equals(lastEditedFile) || !knownfiles.containsKey(getPortableFilePath(editedFile))) {
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
		if (Activator.isInDebugMode) {
			System.out.println("Change: " + textChunk); //$NON-NLS-1$
		}
		log(textChunk);
	}

	public void logConflictEditorTextEvent(TextEvent event, String editorID, boolean isUndoing, boolean isRedoing) {
		//Use DocumentEvent to get correct, file-based offsets (which do not depend on expanding/collapsing of import statements,methods,etc.)
		DocumentEvent documentEvent= event.getDocumentEvent(); //should never be null in this method
		TextChunk textChunk= null;
		if (isUndoing) {
			textChunk= new TextChunk(CONFLICT_EDITOR_TEXT_CHANGE_UNDONE_SYMBOL);
		} else if (isRedoing) {
			textChunk= new TextChunk(CONFLICT_EDITOR_TEXT_CHANGE_REDONE_SYMBOL);
		} else {
			textChunk= new TextChunk(CONFLICT_EDITOR_TEXT_CHANGE_PERFORMED_SYMBOL);
		}
		textChunk.append(editorID);
		String replacedText= event.getReplacedText() == null ? "" : event.getReplacedText(); //$NON-NLS-1$
		textChunk.append(replacedText);
		textChunk.append(documentEvent.getText());
		textChunk.append(documentEvent.getOffset());
		textChunk.append(documentEvent.getLength());
		textChunk.append(System.currentTimeMillis());
		if (Activator.isInDebugMode) {
			System.out.println("Conflict editor change: " + textChunk); //$NON-NLS-1$
		}
		log(textChunk);
	}

	private void logEditedFile() {
		TextChunk textChunk= new TextChunk(FILE_EDIT_SYMBOL);
		textChunk.append(getPortableFilePath(lastEditedFile));
		textChunk.append(System.currentTimeMillis());
		if (Activator.isInDebugMode) {
			System.out.println("File edited: " + textChunk); //$NON-NLS-1$
		}
		log(textChunk);
	}

	public void logOpenedConflictEditor(String editorID, String initialContent, IFile editedFile) {
		TextChunk textChunk= new TextChunk(CONFLICT_EDITOR_OPENED_SYMBOL);
		textChunk.append(editorID);
		textChunk.append(getPortableFilePath(editedFile));
		textChunk.append(initialContent);
		textChunk.append(System.currentTimeMillis());
		if (Activator.isInDebugMode) {
			System.out.println("Conflict editor opened: " + textChunk); //$NON-NLS-1$
		}
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
			textChunk.append(getPortableFilePath(file));
			textChunk.append(System.currentTimeMillis());
			if (Activator.isInDebugMode) {
				System.out.println("File saved: " + textChunk); //$NON-NLS-1$
			}
			log(textChunk);
		}
	}

	public void logSavedConflictEditors(Set<String> savedConflictEditorIDs) {
		for (String conflictEditorID : savedConflictEditorIDs) {
			TextChunk textChunk= new TextChunk(CONFLICT_EDITOR_SAVED_SYMBOL);
			textChunk.append(conflictEditorID);
			textChunk.append(System.currentTimeMillis());
			if (Activator.isInDebugMode) {
				System.out.println("Conflict editor saved: " + textChunk); //$NON-NLS-1$
			}
			log(textChunk);
		}
	}

	public void logExternallyModifiedFiles(Set<IFile> externallyModifiedFiles) {
		for (IFile file : externallyModifiedFiles) {
			TextChunk textChunk= new TextChunk(FILE_EXTERNALLY_MODIFIED_SYMBOL);
			textChunk.append(getPortableFilePath(file));
			textChunk.append(System.currentTimeMillis());
			if (Activator.isInDebugMode) {
				System.out.println("File externally modified: " + textChunk); //$NON-NLS-1$
			}
			log(textChunk);
		}
	}

	public void logUpdatedFiles(Set<IFile> updatedFiles) {
		for (IFile file : updatedFiles) {
			TextChunk textChunk= new TextChunk(FILE_UPDATED_SYMBOL);
			textChunk.append(getPortableFilePath(file));
			textChunk.append(System.currentTimeMillis());
			if (Activator.isInDebugMode) {
				System.out.println("File updated: " + textChunk); //$NON-NLS-1$
			}
			log(textChunk);
		}
	}

	public void logInitiallyCommittedFiles(Set<IFile> initiallyCommittedFiles) {
		logCommit(initiallyCommittedFiles, true);
	}

	public void logCommittedFiles(Set<IFile> committedFiles) {
		logCommit(committedFiles, false);
	}

	/**
	 * Log that files were committed and capture snapshots of them.
	 * 
	 * @param committedFiles
	 * @param isInitialCommit
	 */
	private void logCommit(Set<IFile> committedFiles, boolean isInitialCommit) {
		if (committedFiles.size() > 0) {
			String commitSymbol= FILE_COMMITTED_SYMBOL;
			String debugMessage= "File committed: "; //$NON-NLS-1$
			if (isInitialCommit) {
				commitSymbol= FILE_INITIALLY_COMMITTED_SYMBOL;
				debugMessage= "File initially committed: "; //$NON-NLS-1$
			}
			for (IFile file : committedFiles) {
				TextChunk textChunk= new TextChunk(commitSymbol);
				String portableFilePath= getPortableFilePath(file);
				textChunk.append(portableFilePath);
				File javaFile= new File(file.getLocation().toOSString());
				textChunk.append(getFileContent(javaFile));
				long timestamp= System.currentTimeMillis();
				textChunk.append(timestamp);
				if (Activator.isInDebugMode) {
					System.out.println(debugMessage + textChunk);
				}
				log(textChunk);
				knownfiles.setProperty(portableFilePath, String.valueOf(timestamp));
			}
			logKnownfiles();
		}
	}

	public void logClosedFile(IFile file) {
		TextChunk textChunk= new TextChunk(FILE_CLOSED_SYMBOL);
		textChunk.append(getPortableFilePath(file));
		textChunk.append(System.currentTimeMillis());
		if (Activator.isInDebugMode) {
			System.out.println("File closed: " + textChunk); //$NON-NLS-1$
		}
		log(textChunk);
	}

	public void logClosedConflictEditor(String editorID) {
		TextChunk textChunk= new TextChunk(CONFLICT_EDITOR_CLOSED_SYMBOL);
		textChunk.append(editorID);
		textChunk.append(System.currentTimeMillis());
		if (Activator.isInDebugMode) {
			System.out.println("Conflcit editor closed: " + textChunk); //$NON-NLS-1$
		}
		log(textChunk);
	}

	public void logRefactoringStarted() {
		TextChunk textChunk= new TextChunk(REFACTORING_STARTED_SYMBOL);
		textChunk.append(System.currentTimeMillis());
		if (Activator.isInDebugMode) {
			System.out.println("Refactoring started: " + textChunk); //$NON-NLS-1$
		}
		log(textChunk);
	}

	@SuppressWarnings("rawtypes")
	public void logRefactoringExecutionEvent(RefactoringExecutionEvent event) {
		RefactoringDescriptorProxy refactoringDescriptorProxy= event.getDescriptor();
		RefactoringDescriptor refactoringDescriptor= refactoringDescriptorProxy.requestDescriptor(new NullProgressMonitor());
		if (Activator.isInDebugMode) {
//			System.out.println("Refactoring descriptor id: " + refactoringDescriptor.getID()); //$NON-NLS-1$
//			System.out.println("Project: " + refactoringDescriptor.getProject()); //$NON-NLS-1$
//			System.out.println("Flags: " + refactoringDescriptor.getFlags()); //$NON-NLS-1$
//			System.out.println("Timestamp: " + refactoringDescriptor.getTimeStamp()); //$NON-NLS-1$
		}
		Map arguments= getRefactoringArguments(refactoringDescriptor);
		Set keys= arguments.keySet();
		if (Activator.isInDebugMode) {
//			for (Object key : keys) {
//				Object value= arguments.get(key);
//				System.out.println("Argument \"" + key + "\" = \"" + value + "\""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
//			}
		}
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
		if (Activator.isInDebugMode) {
			System.out.println("Refactoring info: " + textChunk); //$NON-NLS-1$
		}
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
			Object removed= knownfiles.remove(getPortableFilePath(file));
			if (removed != null) {
				hasChanged= true;
			}
		}
		if (hasChanged) {
			logKnownfiles();
		}
	}

	public void ensureIsKnownFile(IFile file) {
		String fileString= getPortableFilePath(file);
		String timestamp= knownfiles.getProperty(fileString);
		if (timestamp == null) { //not found
			knownfiles.setProperty(fileString, String.valueOf(System.currentTimeMillis()));
			logKnownfiles();
			//save the content of a previously unknown file
			File javaFile= new File(file.getLocation().toOSString());
			if (javaFile.exists()) { //Actually, should always exist here
				TextChunk textChunk= new TextChunk(FILE_NEW_SYMBOL);
				textChunk.append(fileString);
				textChunk.append(getFileContent(javaFile));
				textChunk.append(System.currentTimeMillis());
				if (Activator.isInDebugMode) {
					System.out.println("New file: " + textChunk); //$NON-NLS-1$
				}
				log(textChunk);
			}
		}
	}

	private String getFileContent(File file) {
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

	/**
	 * Start writing into a temporary log file
	 */
	public synchronized void commitStarted() {
		if (Activator.isInDebugMode) {
			System.out.println("START COMMIT"); //$NON-NLS-1$
		}
		IPath tempLogFilePath= LOGGER_PATH.append("t" + System.currentTimeMillis() + ".txt"); //$NON-NLS-1$ //$NON-NLS-2$
		currentLogFile= new File(tempLogFilePath.toOSString());
		try {
			currentLogFile.createNewFile();
		} catch (IOException e) {
			logExceptionToErrorLog(e, Messages.Logger_CreateTempLogFileException);
		}
	}

	/**
	 * Switch back to the main log and append to it whatever was written in the temporary file, then
	 * erase the temporary file
	 */
	public synchronized void commitCompleted() {
		if (Activator.isInDebugMode) {
			System.out.println("END COMMIT"); //$NON-NLS-1$
		}
		File tempFile= currentLogFile;
		currentLogFile= mainLogFile;
		String tempContent= getFileContent(tempFile);
		log(tempContent);
		tempFile.delete();
	}

	private synchronized void log(CharSequence text) {
		BufferedWriter logFileWriter= null;
		try {
			if (Activator.isInDebugMode) {
				System.out.println("Before: " + currentLogFile.length()); //$NON-NLS-1$
			}
			logFileWriter= new BufferedWriter(new FileWriter(currentLogFile, true));
			logFileWriter.append(text);
			logFileWriter.flush();
			if (Activator.isInDebugMode) {
				System.out.println("After: " + currentLogFile.length()); //$NON-NLS-1$
			}
		} catch (IOException e) {
			logExceptionToErrorLog(e, Messages.Logger_AppendLogFileException);
		} finally {
			if (logFileWriter != null) {
				try {
					logFileWriter.close();
				} catch (IOException e) {
					//do nothing
				}
			}
		}
	}

	private synchronized void logKnownfiles() {
		if (Activator.isInDebugMode) {
			System.out.println("logKnownfiles"); //$NON-NLS-1$
		}
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

	public static String getPortableFilePath(IFile file) {
		return file.getFullPath().toPortableString();
	}

}
