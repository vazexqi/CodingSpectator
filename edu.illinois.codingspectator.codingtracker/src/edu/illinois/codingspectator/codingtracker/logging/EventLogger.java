/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.logging;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.history.RefactoringExecutionEvent;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryService;

import edu.illinois.codingspectator.codingtracker.Messages;
import edu.illinois.codingspectator.codingtracker.helpers.Debugger;
import edu.illinois.codingspectator.codingtracker.helpers.LoggerHelper;

/**
 * 
 * @author Stas Negara
 * 
 * 
 */
@SuppressWarnings("restriction")
public class EventLogger {

	private static volatile EventLogger loggerInstance= null;

	static final String FEATURE_VERSION= RefactoringHistoryService.getFeatureVersion().toString();

	private static final KnownfilesLogger knownfilesLogger= KnownfilesLogger.getInstance();

	private static final TextLogger textLogger= TextLogger.getInstance();

	private IFile lastEditedFile= null;


	public static EventLogger getInstance() {
		if (loggerInstance == null) {
			loggerInstance= new EventLogger();
		}
		return loggerInstance;
	}

	private EventLogger() {
		TextChunk textChunk= new TextChunk(Symbols.ECLIPSE_SESSION_SYMBOL);
		textChunk.append(System.currentTimeMillis());
		textLogger.log(textChunk);
	}

	public void logTextEvent(TextEvent event, IFile editedFile, boolean isUndoing, boolean isRedoing) {
		if (!editedFile.equals(lastEditedFile) || !knownfilesLogger.isFileKnown(editedFile)) {
			lastEditedFile= editedFile;
			ensureIsKnownFile(lastEditedFile);
			logEditedFile();
		}
		Debugger.debugTextEvent(event);
		TextChunk textChunk= null;
		if (isUndoing) {
			textChunk= new TextChunk(Symbols.TEXT_CHANGE_UNDONE_SYMBOL);
		} else if (isRedoing) {
			textChunk= new TextChunk(Symbols.TEXT_CHANGE_REDONE_SYMBOL);
		} else {
			textChunk= new TextChunk(Symbols.TEXT_CHANGE_PERFORMED_SYMBOL);
		}
		populateTextEventChunk(textChunk, event);
		Debugger.debugTextChunk("Change: ", textChunk);
		textLogger.log(textChunk);
	}

	public void logConflictEditorTextEvent(TextEvent event, String editorID, boolean isUndoing, boolean isRedoing) {
		TextChunk textChunk= null;
		if (isUndoing) {
			textChunk= new TextChunk(Symbols.CONFLICT_EDITOR_TEXT_CHANGE_UNDONE_SYMBOL);
		} else if (isRedoing) {
			textChunk= new TextChunk(Symbols.CONFLICT_EDITOR_TEXT_CHANGE_REDONE_SYMBOL);
		} else {
			textChunk= new TextChunk(Symbols.CONFLICT_EDITOR_TEXT_CHANGE_PERFORMED_SYMBOL);
		}
		textChunk.append(editorID);
		populateTextEventChunk(textChunk, event);
		Debugger.debugTextChunk("Conflict editor change: ", textChunk);
		textLogger.log(textChunk);
	}

	private void populateTextEventChunk(TextChunk textChunk, TextEvent event) {
		//TODO: Logging the replaced text is redundant, is it really needed? 
		//E.g. it could be used during the replay phase to check that the replaced text corresponds to the file text at this offset. 
		String replacedText= event.getReplacedText() == null ? "" : event.getReplacedText();
		textChunk.append(replacedText);
		//Use DocumentEvent to get correct, file-based offsets (which do not depend on expanding/collapsing of import statements,methods,etc.)
		DocumentEvent documentEvent= event.getDocumentEvent(); //should never be null in this method
		textChunk.append(documentEvent.getText());
		textChunk.append(documentEvent.getOffset());
		textChunk.append(documentEvent.getLength());
		textChunk.append(System.currentTimeMillis());
	}

	private void logEditedFile() {
		TextChunk textChunk= new TextChunk(Symbols.FILE_EDIT_SYMBOL);
		textChunk.append(LoggerHelper.getPortableFilePath(lastEditedFile));
		textChunk.append(System.currentTimeMillis());
		Debugger.debugTextChunk("File edited: ", textChunk);
		textLogger.log(textChunk);
	}

	public void logOpenedConflictEditor(String editorID, String initialContent, IFile editedFile) {
		TextChunk textChunk= new TextChunk(Symbols.CONFLICT_EDITOR_OPENED_SYMBOL);
		textChunk.append(editorID);
		textChunk.append(LoggerHelper.getPortableFilePath(editedFile));
		textChunk.append(initialContent);
		textChunk.append(System.currentTimeMillis());
		Debugger.debugTextChunk("Conflict editor opened: ", textChunk);
		textLogger.log(textChunk);
	}

	public void logSavedFiles(Set<IFile> savedFiles, boolean isRefactoring) {
		for (IFile file : savedFiles) {
			TextChunk textChunk;
			if (isRefactoring) {
				textChunk= new TextChunk(Symbols.FILE_REFACTORED_SAVED_SYMBOL);
			} else {
				textChunk= new TextChunk(Symbols.FILE_SAVED_SYMBOL);
			}
			textChunk.append(LoggerHelper.getPortableFilePath(file));
			textChunk.append(System.currentTimeMillis());
			Debugger.debugTextChunk("File saved: ", textChunk);
			textLogger.log(textChunk);
		}
	}

	public void logSavedConflictEditors(Set<String> savedConflictEditorIDs) {
		for (String conflictEditorID : savedConflictEditorIDs) {
			TextChunk textChunk= new TextChunk(Symbols.CONFLICT_EDITOR_SAVED_SYMBOL);
			textChunk.append(conflictEditorID);
			textChunk.append(System.currentTimeMillis());
			Debugger.debugTextChunk("Conflict editor saved: ", textChunk);
			textLogger.log(textChunk);
		}
	}

	public void logExternallyModifiedFiles(Set<IFile> externallyModifiedFiles) {
		for (IFile file : externallyModifiedFiles) {
			TextChunk textChunk= new TextChunk(Symbols.FILE_EXTERNALLY_MODIFIED_SYMBOL);
			textChunk.append(LoggerHelper.getPortableFilePath(file));
			textChunk.append(System.currentTimeMillis());
			Debugger.debugTextChunk("File externally modified: ", textChunk);
			textLogger.log(textChunk);
		}
	}

	public void logUpdatedFiles(Set<IFile> updatedFiles) {
		for (IFile file : updatedFiles) {
			TextChunk textChunk= new TextChunk(Symbols.FILE_UPDATED_SYMBOL);
			textChunk.append(LoggerHelper.getPortableFilePath(file));
			textChunk.append(System.currentTimeMillis());
			Debugger.debugTextChunk("File updated: ", textChunk);
			textLogger.log(textChunk);
		}
	}

	/**
	 * Logs the committed files including their content.
	 * 
	 * @param committedFiles
	 * @param isInitialCommit
	 */
	public void logCommittedFiles(Set<IFile> committedFiles, boolean isInitialCommit) {
		if (committedFiles.size() > 0) {
			String commitSymbol= Symbols.FILE_COMMITTED_SYMBOL;
			String debugMessage= "File committed: ";
			if (isInitialCommit) {
				commitSymbol= Symbols.FILE_INITIALLY_COMMITTED_SYMBOL;
				debugMessage= "File initially committed: ";
			}
			for (IFile file : committedFiles) {
				TextChunk textChunk= new TextChunk(commitSymbol);
				textChunk.append(LoggerHelper.getPortableFilePath(file));
				File javaFile= new File(file.getLocation().toOSString());
				textChunk.append(LoggerHelper.getFileContent(javaFile));
				textChunk.append(System.currentTimeMillis());
				Debugger.debugTextChunk(debugMessage, textChunk);
				textLogger.log(textChunk);
				knownfilesLogger.addKnownfile(file);
			}
			knownfilesLogger.logKnownfiles();
		}
	}

	public void logClosedFile(IFile file) {
		TextChunk textChunk= new TextChunk(Symbols.FILE_CLOSED_SYMBOL);
		textChunk.append(LoggerHelper.getPortableFilePath(file));
		textChunk.append(System.currentTimeMillis());
		Debugger.debugTextChunk("File closed: ", textChunk);
		textLogger.log(textChunk);
	}

	public void logClosedConflictEditor(String editorID) {
		TextChunk textChunk= new TextChunk(Symbols.CONFLICT_EDITOR_CLOSED_SYMBOL);
		textChunk.append(editorID);
		textChunk.append(System.currentTimeMillis());
		Debugger.debugTextChunk("Conflict editor closed: ", textChunk);
		textLogger.log(textChunk);
	}

	public void logRefactoringStarted() {
		TextChunk textChunk= new TextChunk(Symbols.REFACTORING_STARTED_SYMBOL);
		textChunk.append(System.currentTimeMillis());
		Debugger.debugTextChunk("Refactoring started: ", textChunk);
		textLogger.log(textChunk);
	}

	@SuppressWarnings("rawtypes")
	public void logRefactoringExecutionEvent(RefactoringExecutionEvent event) {
		RefactoringDescriptorProxy refactoringDescriptorProxy= event.getDescriptor();
		RefactoringDescriptor refactoringDescriptor= refactoringDescriptorProxy.requestDescriptor(new NullProgressMonitor());
		Debugger.debugRefactoringDescriptor(refactoringDescriptor);
		TextChunk textChunk= null;
		switch (event.getEventType()) {
			case RefactoringExecutionEvent.PERFORMED:
				textChunk= new TextChunk(Symbols.REFACTORING_PERFORMED_SYMBOL);
				break;
			case RefactoringExecutionEvent.REDONE:
				textChunk= new TextChunk(Symbols.REFACTORING_REDONE_SYMBOL);
				break;
			case RefactoringExecutionEvent.UNDONE:
				textChunk= new TextChunk(Symbols.REFACTORING_UNDONE_SYMBOL);
				break;
			default:
				Exception e= new RuntimeException();
				LoggerHelper.logExceptionToErrorLog(e, Messages.Logger_UnrecognizedRefactoringType + event.getEventType());
		}
		textChunk.append(refactoringDescriptor.getID());
		textChunk.append(refactoringDescriptor.getProject());
		textChunk.append(refactoringDescriptor.getFlags());
		Map arguments= LoggerHelper.getRefactoringArguments(refactoringDescriptor);
		if (arguments != null) {
			Set keys= arguments.keySet();
			textChunk.append(keys.size());
			for (Object key : keys) {
				Object value= arguments.get(key);
				textChunk.append(key);
				textChunk.append(value);
			}
		} else {
			textChunk.append(0);
		}
		textChunk.append(refactoringDescriptor.getTimeStamp());
		Debugger.debugTextChunk("Refactoring info: ", textChunk);
		textLogger.log(textChunk);
	}

	public void removeKnownFiles(Set<IFile> files) {
		boolean hasChanged= false;
		for (IFile file : files) {
			Object removed= knownfilesLogger.removeKnownfile(file);
			if (removed != null) {
				hasChanged= true;
			}
		}
		if (hasChanged) {
			knownfilesLogger.logKnownfiles();
		}
	}

	private void ensureIsKnownFile(IFile file) {
		//TODO: Is creating a new HashSet for a single file too expensive?
		Set<IFile> files= new HashSet<IFile>(1);
		files.add(file);
		ensureAreKnownFiles(files);
	}

	public void ensureAreKnownFiles(Set<IFile> files) {
		boolean hasChanged= false;
		for (IFile file : files) {
			if (!knownfilesLogger.isFileKnown(file)) {
				knownfilesLogger.addKnownfile(file);
				hasChanged= true;
				//save the content of a previously unknown file
				File javaFile= new File(file.getLocation().toOSString());
				if (javaFile.exists()) { //Actually, should always exist here
					TextChunk textChunk= new TextChunk(Symbols.FILE_NEW_SYMBOL);
					textChunk.append(LoggerHelper.getPortableFilePath(file));
					textChunk.append(LoggerHelper.getFileContent(javaFile));
					textChunk.append(System.currentTimeMillis());
					Debugger.debugTextChunk("New file: ", textChunk);
					textLogger.log(textChunk);
				}
			}
		}
		if (hasChanged) {
			knownfilesLogger.logKnownfiles();
		}
	}

	public void commitStarted() {
		textLogger.commitStarted();
	}

	public void commitCompleted() {
		textLogger.commitCompleted();
	}

}
