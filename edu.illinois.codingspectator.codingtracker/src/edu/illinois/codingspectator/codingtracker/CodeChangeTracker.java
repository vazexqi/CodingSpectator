/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.commands.operations.TriggeredOperations;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.history.IRefactoringExecutionListener;
import org.eclipse.ltk.core.refactoring.history.RefactoringExecutionEvent;
import org.eclipse.ltk.internal.core.refactoring.UndoableOperation2ChangeAdapter;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;

import edu.illinois.codingspectator.codingtracker.listeners.PartListener;

/**
 * 
 * This class registers listeners for all interesting events; receives and processes these events:
 * it extracts the required information from some complex events (e.g. IResourceChangeEvent). Also,
 * it keeps track of the current changing state (e.g. undoing, redoing, refactoring).
 * 
 * @author Stas Negara
 * @author Mohsen Vakilian - Extracted PartListener, EditorHelper and FileProperties from this
 *         class.
 * 
 */
@SuppressWarnings("restriction")
public class CodeChangeTracker implements ISelectionListener, ITextListener, IRefactoringExecutionListener,
											IResourceChangeListener, IOperationHistoryListener {

	private static CodeChangeTracker trackerInstance;

	private final Logger logger;

	UserSessionState userSessionState= new UserSessionState(null, null, null, new HashSet<CompareEditor>(), new HashSet<CompareEditor>(), false, false, false, false, null,
			Collections.synchronizedSet(new HashSet<IFile>()));

	public static CodeChangeTracker getInstance() {
		if (trackerInstance == null) {
			trackerInstance= new CodeChangeTracker();
		}
		return trackerInstance;
	}

	private CodeChangeTracker() {
		logger= Logger.getInstance();
	}

	/**
	 * Registers the listeners.
	 */
	public void start() {
		if (Activator.isInDebugMode) {
			System.out.println("Early startup");
		}
		ResourcesPlugin.getWorkspace().addResourceChangeListener(trackerInstance, IResourceChangeEvent.POST_CHANGE);
		OperationHistoryFactory.getOperationHistory().addOperationHistoryListener(trackerInstance);
		RefactoringCore.getHistoryService().addExecutionListener(trackerInstance);

		setActiveWorkbench();
		registerSelectionListener();
		registerPartListener();
	}

	private void registerPartListener() {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				//TODO: Is it too heavy-weight? Did not notice any additional lag even on a slow machine.  
				while (!userSessionState.isPartListenerRegistered()) {
					IWorkbenchPage activePage= userSessionState.getActiveWorkbenchWindow().getActivePage();
					if (activePage != null) {
						activePage.addPartListener(new PartListener(userSessionState));
						userSessionState.setPartListenerRegistered(true);
					}
				}
			}
		});
	}

	private void setActiveWorkbench() {
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				userSessionState.setActiveWorkbenchWindow(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
				if (userSessionState.getActiveWorkbenchWindow() == null) {
					Exception e= new RuntimeException();
					Logger.logExceptionToErrorLog(e, Messages.CodeChangeTracker_FailedToGetActiveWorkbenchWindow);
				}
			}
		});
	}

	private void registerSelectionListener() {
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				userSessionState.getActiveWorkbenchWindow().getSelectionService().addSelectionListener(trackerInstance);
			}
		});
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (Activator.isInDebugMode) {
			System.out.println("Part=" + part.getClass().getName());
		}
		IFile newFile= null;
		ISourceViewer sourceViewer= null;
		if (part instanceof CompareEditor) {
			CompareEditor compareEditor= (CompareEditor)part;
			newFile= EditorHelper.getEditorJavaFile(compareEditor);
			sourceViewer= EditorHelper.getEditorSourceViewer(compareEditor);
		} else if (part instanceof AbstractDecoratedTextEditor) {
			AbstractDecoratedTextEditor editor= (AbstractDecoratedTextEditor)part;
			newFile= EditorHelper.getEditorJavaFile(editor);
			sourceViewer= EditorHelper.getEditorSourceViewer(editor);
		}
		if (newFile != null) {
			userSessionState.setCurrentEditor((EditorPart)part); //Should be EditorPart if newFile != null
			addEditor(userSessionState.getCurrentEditor(), newFile);
			if (!newFile.equals(userSessionState.getCurrentFile())) {
				userSessionState.setCurrentFile(newFile);
				if (Activator.isInDebugMode) {
					System.out.println("File:\"" + Logger.getPortableFilePath(userSessionState.getCurrentFile()) + "\"");
				}
			}
			if (userSessionState.getListenedViewer() != null) {
				userSessionState.getListenedViewer().removeTextListener(trackerInstance);
			}
			userSessionState.setListenedViewer(sourceViewer);
			if (userSessionState.getListenedViewer() != null) {
				userSessionState.getListenedViewer().addTextListener(trackerInstance);
			}
		}
	}

	private void addEditor(EditorPart editor, IFile editedFile) {
		if (EditorHelper.isConflictEditor(editor)) {
			CompareEditor compareEditor= (CompareEditor)editor;
			if (!userSessionState.getOpenConflictEditors().contains(compareEditor)) {
				userSessionState.getOpenConflictEditors().add(compareEditor);
				userSessionState.getDirtyConflictEditors().add(compareEditor); //conflict editors are always dirty from the start
				logger.logOpenedConflictEditor(EditorHelper.getConflictEditorID(compareEditor), EditorHelper.getConflictEditorInitialContent(compareEditor), editedFile);
			}
		}
	}

	@Override
	public void textChanged(TextEvent event) {
		DocumentEvent documentEvent= event.getDocumentEvent();
		if (documentEvent != null && !userSessionState.isRefactoring()) {
			if (EditorHelper.isConflictEditor(userSessionState.getCurrentEditor())) {
				CompareEditor compareEditor= (CompareEditor)userSessionState.getCurrentEditor();
				userSessionState.getDirtyConflictEditors().add(compareEditor);
				logger.logConflictEditorTextEvent(event, EditorHelper.getConflictEditorID(compareEditor), userSessionState.isUndoing(), userSessionState.isRedoing());
			} else {
				userSessionState.getDirtyFiles().add(userSessionState.getCurrentFile());
				logger.logTextEvent(event, userSessionState.getCurrentFile(), userSessionState.isUndoing(), userSessionState.isRedoing());
			}
		}
	}

	@Override
	public void executionNotification(RefactoringExecutionEvent event) {
		int eventType= event.getEventType();
		if (eventType == RefactoringExecutionEvent.ABOUT_TO_PERFORM || eventType == RefactoringExecutionEvent.ABOUT_TO_REDO ||
				eventType == RefactoringExecutionEvent.ABOUT_TO_UNDO) {
			userSessionState.setRefactoring(true);
			logger.logRefactoringStarted();
		} else {
			userSessionState.setRefactoring(false);
			logger.logRefactoringExecutionEvent(event);
		}
	}

	@Override
	public void historyNotification(OperationHistoryEvent event) {
		int eventType= event.getEventType();
		if (eventType == OperationHistoryEvent.ABOUT_TO_UNDO) {
			userSessionState.setUndoing(true);
		} else {
			userSessionState.setUndoing(false);
		}
		if (eventType == OperationHistoryEvent.ABOUT_TO_REDO) {
			userSessionState.setRedoing(true);
		} else {
			userSessionState.setRedoing(false);
		}
		if (eventType == OperationHistoryEvent.UNDONE || eventType == OperationHistoryEvent.REDONE) {
			if (userSessionState.getCurrentEditor() != null && !EditorHelper.isConflictEditor(userSessionState.getCurrentEditor())) { //conflict editors remain dirty until saved
				if (userSessionState.getCurrentEditor().isDirty()) {
					userSessionState.getDirtyFiles().add(userSessionState.getCurrentFile());
				} else {
					userSessionState.getDirtyFiles().remove(userSessionState.getCurrentFile());
				}
			}
		}
		if (eventType == OperationHistoryEvent.ABOUT_TO_EXECUTE || (eventType == OperationHistoryEvent.ABOUT_TO_REDO) ||
				eventType == OperationHistoryEvent.ABOUT_TO_UNDO) {
			IUndoableOperation undoableOperation= event.getOperation();
			if (undoableOperation instanceof TriggeredOperations) {
				IUndoableOperation triggeringOperation= ((TriggeredOperations)undoableOperation).getTriggeringOperation();
				if (triggeringOperation instanceof UndoableOperation2ChangeAdapter) {
					Object[] affectedObjects= ((UndoableOperation2ChangeAdapter)triggeringOperation).getAllAffectedObjects();
					if (affectedObjects != null) {
						for (Object affectedObject : affectedObjects) {
							if (affectedObject instanceof CompilationUnit) {
								IResource resource= ((CompilationUnit)affectedObject).getResource();
								if (resource instanceof IFile) { //Could it be something else?
									IFile file= (IFile)resource;
									if (Activator.isInDebugMode) {
										System.out.println("File to be modified:" + Logger.getPortableFilePath(file));
									}
									logger.ensureIsKnownFile(file);
								}
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta delta= event.getDelta();
		if (delta != null) { //why could it be null?
			final Set<IFile> addedJavaFiles= new HashSet<IFile>();
			final Set<IFile> changedJavaFiles= new HashSet<IFile>();
			final Set<IFile> removedJavaFiles= new HashSet<IFile>();
			final Set<IFile> svnAddedJavaFiles= new HashSet<IFile>();
			final Set<IFile> svnChangedJavaFiles= new HashSet<IFile>();
			final Set<String> svnEntriesChangeSet= new HashSet<String>();
			try {
				delta.accept(new IResourceDeltaVisitor() {

					@Override
					public boolean visit(IResourceDelta delta) throws CoreException {
						IResource resource= delta.getResource();
						if (resource.getType() == IResource.FILE) {
							IFile file= (IFile)resource;
							if (file.getName().equals("entries")) { //$NON-NLS-1$
								svnEntriesChangeSet.add("yes"); //$NON-NLS-1$
							} else {
								String fileExtension= file.getFileExtension(); //may be null
								if ("java".equals(fileExtension)) { //$NON-NLS-1$
									switch (delta.getKind()) {
										case IResourceDelta.ADDED:
											addedJavaFiles.add(file);
											break;
										case IResourceDelta.REMOVED:
											removedJavaFiles.add(file);
											break;
										case IResourceDelta.CHANGED:
											if ((delta.getFlags() & IResourceDelta.CONTENT) != 0) {
												changedJavaFiles.add(file);
											}
											break;
									}
								} else if ("svn-base".equals(fileExtension)) { //$NON-NLS-1$
									switch (delta.getKind()) {
										case IResourceDelta.ADDED:
											IFile javaSourceFile= getJavaSourceFileForSVNFile(file);
											if (javaSourceFile != null) {
												svnAddedJavaFiles.add(javaSourceFile);
											}
											break;
										case IResourceDelta.CHANGED:
											if ((delta.getFlags() & IResourceDelta.CONTENT) != 0) {
												javaSourceFile= getJavaSourceFileForSVNFile(file);
												if (javaSourceFile != null) {
													svnChangedJavaFiles.add(javaSourceFile);
												}
											}
											break;
									}
								}
							}
						}
						return true;
					}
				}, IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS); //to see changes in .svn folder
			} catch (CoreException e) {
				Logger.logExceptionToErrorLog(e, Messages.CodeChangeTracker_FailedToVisitResourceDelta);
			}
			final Set<IFile> savedJavaFiles= new HashSet<IFile>();
			//Actually, should not be more than one per resourceChanged notification
			final Set<String> savedConflictEditorIDs= new HashSet<String>();
			final Set<IFile> externallyModifiedJavaFiles= new HashSet<IFile>();
			final Set<IFile> updatedJavaFiles= new HashSet<IFile>();
			final Set<IFile> initiallyCommittedJavaFiles= new HashSet<IFile>();
			final Set<IFile> committedJavaFiles= new HashSet<IFile>();
			for (IFile file : svnChangedJavaFiles) {
				if (changedJavaFiles.contains(file)) {
					updatedJavaFiles.add(file); //if both the java file and its svn storage have changed, then its an update
				} else {
					committedJavaFiles.add(file); //if only svn storage of a java file has changed, its a commit
				}
			}
			for (IFile file : svnAddedJavaFiles) {
				if (!addedJavaFiles.contains(file)) { //if only svn storage was added for a file, its an initial commit
					initiallyCommittedJavaFiles.add(file);
				}
			}
			//Detect files saved from a conflict editor and remove them from changedJavaFiles
			//such that they are not considered for a regular save
			IWorkbenchPage activePage= userSessionState.getActiveWorkbenchWindow().getActivePage();
			if (activePage != null) {
				IEditorReference[] editorReferences= activePage.getEditorReferences();
				for (IEditorReference editorReference : editorReferences) {
					IEditorPart editor= editorReference.getEditor(false);
					if (editor != null && EditorHelper.isConflictEditor(editor) && !editor.isDirty()) {
						CompareEditor compareEditor= (CompareEditor)editor;
						if (userSessionState.getDirtyConflictEditors().contains(compareEditor)) {
							userSessionState.getDirtyConflictEditors().remove(compareEditor);
							savedConflictEditorIDs.add(EditorHelper.getConflictEditorID(compareEditor));
							changedJavaFiles.remove(EditorHelper.getEditorJavaFile(compareEditor));
						}
					}
				}
			}
			boolean isSVNEntriesChanged= svnEntriesChangeSet.size() > 0;
			for (IFile file : changedJavaFiles) {
				if (!updatedJavaFiles.contains(file)) { //updated files are neither saved nor externally modified
					if (userSessionState.isRefactoring() || userSessionState.getDirtyFiles().contains(file) && !isSVNEntriesChanged) {
						savedJavaFiles.add(file);
					} else {
						externallyModifiedJavaFiles.add(file);
					}
				}
				//TODO: Removing from dirty files when updated or changed externally may cause subsequent save to be treated as an
				//external modification. Is it ok (e.g. this can be detected and filtered out during the replay phase)?
				userSessionState.getDirtyFiles().remove(file);
			}
			userSessionState.getDirtyFiles().removeAll(removedJavaFiles);
			logger.logSavedFiles(savedJavaFiles, userSessionState.isRefactoring());
			logger.logSavedConflictEditors(savedConflictEditorIDs);
			logger.logExternallyModifiedFiles(externallyModifiedJavaFiles);
			logger.logUpdatedFiles(updatedJavaFiles);
			logger.logInitiallyCommittedFiles(initiallyCommittedJavaFiles);
			logger.logCommittedFiles(committedJavaFiles);
			removedJavaFiles.addAll(updatedJavaFiles); //updated files become unknown (like removed)
			removedJavaFiles.addAll(externallyModifiedJavaFiles); //externally modified files become unknown
			logger.removeKnownFiles(removedJavaFiles);
		}
	}

	/**
	 * Returns null if there is no corresponding Java source file (e.g. when the SVN file is not
	 * from text-base folder).
	 * 
	 * @param svnFile
	 * @return
	 */
	private IFile getJavaSourceFileForSVNFile(IFile svnFile) {
		IFile javaSourceFile= null;
		String fileName= svnFile.getName();
		if (fileName.endsWith(".java.svn-base")) { //$NON-NLS-1$
			IPath fileFullPath= svnFile.getFullPath();
			String parentDir= fileFullPath.segment(fileFullPath.segmentCount() - 2);
			if (parentDir.equals("text-base")) { //$NON-NLS-1$
				String javaSourceFileName= fileName.substring(0, fileName.lastIndexOf(".")); //$NON-NLS-1$
				IPath javaSourceFilePath= fileFullPath.removeLastSegments(3).append(javaSourceFileName);
				javaSourceFile= ResourcesPlugin.getWorkspace().getRoot().getFile(javaSourceFilePath);
			}
		}
		return javaSourceFile;
	}

	// beginning og move to FileProperties
	private boolean isJavaFile(IFile file) {
		String fileExtension= file.getFileExtension();
		if (fileExtension != null && fileExtension.equals("java")) { //$NON-NLS-1$
			return true;
		}
		return false;
	}

	//end of move to FileProperties


}
