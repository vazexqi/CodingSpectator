/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker.recording;

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
import edu.illinois.codingspectator.codingtracker.helpers.RecorderHelper;
import edu.illinois.codingspectator.codingtracker.operations.OperationFactory;

/**
 * 
 * @author Stas Negara
 * 
 * 
 */
@SuppressWarnings("restriction")
public class EventRecorder {

	private static volatile EventRecorder recorderInstance= null;

	static final String FEATURE_VERSION= RefactoringHistoryService.getFeatureVersion().toString();

	private static final KnownfilesRecorder knownfilesRecorder= KnownfilesRecorder.getInstance();

	private static final TextRecorder textRecorder= TextRecorder.getInstance();

	private IFile lastEditedFile= null;


	public static EventRecorder getInstance() {
		if (recorderInstance == null) {
			recorderInstance= new EventRecorder();
		}
		return recorderInstance;
	}

	private EventRecorder() {
		OperationFactory.createStartEclipseOperation().serialize(textRecorder);
	}

	public void recordTextEvent(TextEvent event, IFile editedFile, boolean isUndoing, boolean isRedoing) {
		if (!editedFile.equals(lastEditedFile) || !knownfilesRecorder.isFileKnown(editedFile)) {
			lastEditedFile= editedFile;
			ensureIsKnownFile(lastEditedFile);
			recordEditedFile();
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
		textRecorder.record(textChunk);
	}

	public void recordConflictEditorTextEvent(TextEvent event, String editorID, boolean isUndoing, boolean isRedoing) {
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
		textRecorder.record(textChunk);
	}

	private void populateTextEventChunk(TextChunk textChunk, TextEvent event) {
		//TODO: Recording the replaced text is redundant, is it really needed? 
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

	private void recordEditedFile() {
		OperationFactory.createFileEditOperation(lastEditedFile).serialize(textRecorder);
	}

	public void recordOpenedConflictEditor(String editorID, String initialContent, IFile editedFile) {
		TextChunk textChunk= new TextChunk(Symbols.CONFLICT_EDITOR_OPENED_SYMBOL);
		textChunk.append(editorID);
		textChunk.append(RecorderHelper.getPortableFilePath(editedFile));
		textChunk.append(initialContent);
		textChunk.append(System.currentTimeMillis());
		Debugger.debugTextChunk("Conflict editor opened: ", textChunk);
		textRecorder.record(textChunk);
	}

	public void recordSavedFiles(Set<IFile> savedFiles, boolean isRefactoring) {
		for (IFile file : savedFiles) {
			TextChunk textChunk;
			if (isRefactoring) {
				textChunk= new TextChunk(Symbols.FILE_REFACTORED_SAVED_SYMBOL);
			} else {
				textChunk= new TextChunk(Symbols.FILE_SAVED_SYMBOL);
			}
			textChunk.append(RecorderHelper.getPortableFilePath(file));
			textChunk.append(System.currentTimeMillis());
			Debugger.debugTextChunk("File saved: ", textChunk);
			textRecorder.record(textChunk);
		}
	}

	public void recordSavedConflictEditors(Set<String> savedConflictEditorIDs) {
		for (String conflictEditorID : savedConflictEditorIDs) {
			TextChunk textChunk= new TextChunk(Symbols.CONFLICT_EDITOR_SAVED_SYMBOL);
			textChunk.append(conflictEditorID);
			textChunk.append(System.currentTimeMillis());
			Debugger.debugTextChunk("Conflict editor saved: ", textChunk);
			textRecorder.record(textChunk);
		}
	}

	public void recordExternallyModifiedFiles(Set<IFile> externallyModifiedFiles) {
		for (IFile file : externallyModifiedFiles) {
			TextChunk textChunk= new TextChunk(Symbols.FILE_EXTERNALLY_MODIFIED_SYMBOL);
			textChunk.append(RecorderHelper.getPortableFilePath(file));
			textChunk.append(System.currentTimeMillis());
			Debugger.debugTextChunk("File externally modified: ", textChunk);
			textRecorder.record(textChunk);
		}
	}

	public void recordUpdatedFiles(Set<IFile> updatedFiles) {
		for (IFile file : updatedFiles) {
			TextChunk textChunk= new TextChunk(Symbols.FILE_UPDATED_SYMBOL);
			textChunk.append(RecorderHelper.getPortableFilePath(file));
			textChunk.append(System.currentTimeMillis());
			Debugger.debugTextChunk("File updated: ", textChunk);
			textRecorder.record(textChunk);
		}
	}

	/**
	 * Records the committed files including their content.
	 * 
	 * @param committedFiles
	 * @param isInitialCommit
	 */
	public void recordCommittedFiles(Set<IFile> committedFiles, boolean isInitialCommit) {
		if (committedFiles.size() > 0) {
			String commitSymbol= Symbols.FILE_COMMITTED_SYMBOL;
			String debugMessage= "File committed: ";
			if (isInitialCommit) {
				commitSymbol= Symbols.FILE_INITIALLY_COMMITTED_SYMBOL;
				debugMessage= "File initially committed: ";
			}
			for (IFile file : committedFiles) {
				TextChunk textChunk= new TextChunk(commitSymbol);
				textChunk.append(RecorderHelper.getPortableFilePath(file));
				File javaFile= new File(file.getLocation().toOSString());
				textChunk.append(RecorderHelper.getFileContent(javaFile));
				textChunk.append(System.currentTimeMillis());
				Debugger.debugTextChunk(debugMessage, textChunk);
				textRecorder.record(textChunk);
				knownfilesRecorder.addKnownfile(file);
			}
			knownfilesRecorder.recordKnownfiles();
		}
	}

	public void recordClosedFile(IFile file) {
		TextChunk textChunk= new TextChunk(Symbols.FILE_CLOSED_SYMBOL);
		textChunk.append(RecorderHelper.getPortableFilePath(file));
		textChunk.append(System.currentTimeMillis());
		Debugger.debugTextChunk("File closed: ", textChunk);
		textRecorder.record(textChunk);
	}

	public void recordClosedConflictEditor(String editorID) {
		TextChunk textChunk= new TextChunk(Symbols.CONFLICT_EDITOR_CLOSED_SYMBOL);
		textChunk.append(editorID);
		textChunk.append(System.currentTimeMillis());
		Debugger.debugTextChunk("Conflict editor closed: ", textChunk);
		textRecorder.record(textChunk);
	}

	public void recordRefactoringStarted() {
		TextChunk textChunk= new TextChunk(Symbols.REFACTORING_STARTED_SYMBOL);
		textChunk.append(System.currentTimeMillis());
		Debugger.debugTextChunk("Refactoring started: ", textChunk);
		textRecorder.record(textChunk);
	}

	@SuppressWarnings("rawtypes")
	public void recordRefactoringExecutionEvent(RefactoringExecutionEvent event) {
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
				Debugger.logExceptionToErrorLog(e, Messages.Recorder_UnrecognizedRefactoringType + event.getEventType());
		}
		textChunk.append(refactoringDescriptor.getID());
		textChunk.append(refactoringDescriptor.getProject());
		textChunk.append(refactoringDescriptor.getFlags());
		Map arguments= RecorderHelper.getRefactoringArguments(refactoringDescriptor);
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
		textRecorder.record(textChunk);
	}

	public void removeKnownFiles(Set<IFile> files) {
		boolean hasChanged= false;
		for (IFile file : files) {
			Object removed= knownfilesRecorder.removeKnownfile(file);
			if (removed != null) {
				hasChanged= true;
			}
		}
		if (hasChanged) {
			knownfilesRecorder.recordKnownfiles();
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
			if (!knownfilesRecorder.isFileKnown(file)) {
				knownfilesRecorder.addKnownfile(file);
				hasChanged= true;
				//save the content of a previously unknown file
				File javaFile= new File(file.getLocation().toOSString());
				if (javaFile.exists()) { //Actually, should always exist here
					TextChunk textChunk= new TextChunk(Symbols.FILE_NEW_SYMBOL);
					textChunk.append(RecorderHelper.getPortableFilePath(file));
					textChunk.append(RecorderHelper.getFileContent(javaFile));
					textChunk.append(System.currentTimeMillis());
					Debugger.debugTextChunk("New file: ", textChunk);
					textRecorder.record(textChunk);
				}
			}
		}
		if (hasChanged) {
			knownfilesRecorder.recordKnownfiles();
		}
	}

	public void commitStarted() {
		textRecorder.commitStarted();
	}

	public void commitCompleted() {
		textRecorder.commitCompleted();
	}

}
