/**
 * This file is licensed under the University of Illinois/NCSA Open Source License. See LICENSE.TXT for details.
 */
package edu.illinois.codingspectator.codingtracker;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.ResourceNode;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
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
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.history.IRefactoringExecutionListener;
import org.eclipse.ltk.core.refactoring.history.RefactoringExecutionEvent;
import org.eclipse.ltk.internal.core.refactoring.UndoableOperation2ChangeAdapter;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.internal.ui.mapping.ModelCompareEditorInput;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;

/**
 * 
 * @author Stas Negara
 * 
 */
@SuppressWarnings("restriction")
public class CodeChangeTracker implements ISelectionListener, ITextListener, IRefactoringExecutionListener,
											IResourceChangeListener, IPartListener, IOperationHistoryListener {

	private static CodeChangeTracker trackerInstance;

	private final Logger logger;

	private IFile currentFile= null;

	private EditorPart currentEditor= null;

	ISourceViewer listenedViewer= null;

	//CompareEditors opened for conflict editing require special handling. They are not editing the original file, but some initial
	//snapshot, which needs to be logged. Also, changes performed in such an editor does not affect other editors, thus need to be logged
	//separately. Moreover, a user can open many such editors for a single file, which means that code changes for every such editor
	//have to be logged separately (i.e. identified with a unique editor's ID).
	private final Set<CompareEditor> openConflictEditors= new HashSet<CompareEditor>();

	private final Set<CompareEditor> dirtyConflictEditors= new HashSet<CompareEditor>();

	private volatile boolean isRefactoring= false;

	private volatile boolean isUndoing= false;

	private volatile boolean isRedoing= false;

	private boolean isPartListenerRegistered= false;

	private volatile IWorkbenchWindow activeWorkbenchWindow= null;

	private Set<IFile> dirtyFiles= Collections.synchronizedSet(new HashSet<IFile>());

	public static CodeChangeTracker getInstance() {
		if (trackerInstance == null) {
			trackerInstance= new CodeChangeTracker();
		}
		return trackerInstance;
	}

	private CodeChangeTracker() {
		logger= Logger.getInstance();
	}

	public void start() {
		if (Activator.isInDebugMode) {
			System.out.println("Early startup");
		}
		ResourcesPlugin.getWorkspace().addResourceChangeListener(trackerInstance, IResourceChangeEvent.POST_CHANGE);
		OperationHistoryFactory.getOperationHistory().addOperationHistoryListener(trackerInstance);
		RefactoringCore.getHistoryService().addExecutionListener(trackerInstance);
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				activeWorkbenchWindow= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (activeWorkbenchWindow == null) {
					Exception e= new RuntimeException();
					Logger.logExceptionToErrorLog(e, Messages.CodeChangeTracker_FailedToGetActiveWorkbenchWindow);
				}
				activeWorkbenchWindow.getSelectionService().addSelectionListener(trackerInstance);
				IWorkbenchPage activePage= activeWorkbenchWindow.getActivePage();
				if (activePage != null) {
					selectionChanged(activePage.getActivePart(), activePage.getSelection());
					activePage.addPartListener(trackerInstance);
					isPartListenerRegistered= true;
				}
			}
		});
		if (!isPartListenerRegistered) {
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					//TODO: Is it too heavy-weight? Did not notice any additional lag even on a slow machine.  
					while (!isPartListenerRegistered) {
						IWorkbenchPage activePage= activeWorkbenchWindow.getActivePage();
						if (activePage != null) {
							activePage.addPartListener(trackerInstance);
							isPartListenerRegistered= true;
						}
					}
				}
			});
		}
	}

	private boolean isConflictEditor(IEditorPart editor) {
		if (!(editor instanceof CompareEditor)) {
			return false;
		}
		//TODO: Maybe some other inputs (not of a conflict editor) are good for tracking and are not ModelCompareEditorInput
		if (((CompareEditor)editor).getEditorInput() instanceof ModelCompareEditorInput) {
			return false;
		}
		return true;
	}

	private String getConflictEditorInitialContent(CompareEditor compareEditor) {
		CompareEditorInput compareEditorInput= (CompareEditorInput)compareEditor.getEditorInput();
		ICompareInput compareInput= (ICompareInput)compareEditorInput.getCompareResult();
		ResourceNode resourceNode= (ResourceNode)compareInput.getLeft();
		return new String(resourceNode.getContent());
	}

	private String getConflictEditorID(CompareEditor compareEditor) {
		String compareEditorString= compareEditor.toString();
		return compareEditorString.substring(compareEditorString.lastIndexOf('@') + 1);
	}

	private IFile getEditorJavaFile(CompareEditor compareEditor) {
		IFile javaFile= null;
		IEditorInput editorInput= compareEditor.getEditorInput();
		if (editorInput instanceof CompareEditorInput) {
			CompareEditorInput compareEditorInput= (CompareEditorInput)editorInput;
			Object compareResult= compareEditorInput.getCompareResult();
			if (compareResult instanceof ICompareInput) {
				ICompareInput compareInput= (ICompareInput)compareResult;
				ITypedElement leftTypedElement= compareInput.getLeft();
				if (leftTypedElement instanceof ResourceNode) {
					ResourceNode resourceNode= (ResourceNode)leftTypedElement;
					IResource resource= resourceNode.getResource();
					if (resource instanceof IFile) {
						IFile file= (IFile)resource;
						if (isJavaFile(file)) {
							javaFile= file;
						}
					}
				}
			}
		}
		return javaFile;
	}

	private IFile getEditorJavaFile(AbstractDecoratedTextEditor editor) {
		IFile javaFile= null;
		IEditorInput editorInput= editor.getEditorInput();
		if (editorInput instanceof FileEditorInput) {
			IFile file= ((FileEditorInput)editorInput).getFile();
			if (isJavaFile(file)) {
				javaFile= file;
			}
		}
		return javaFile;
	}

	private ISourceViewer getEditorSourceViewer(CompareEditor compareEditor) {
		ISourceViewer sourceViewer= null;
		IEditorInput editorInput= compareEditor.getEditorInput();
		if (editorInput instanceof CompareEditorInput) {
			CompareEditorInput compareEditorInput= (CompareEditorInput)editorInput;
			Viewer contentViewer= compareEditorInput.getContentViewer();
			if (contentViewer instanceof TextMergeViewer) {
				sourceViewer= ((TextMergeViewer)contentViewer).getLeftViewer();
			}
		}
		return sourceViewer;
	}

	private ISourceViewer getEditorSourceViewer(AbstractDecoratedTextEditor editor) {
		return editor.getHackedViewer();
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
			newFile= getEditorJavaFile(compareEditor);
			sourceViewer= getEditorSourceViewer(compareEditor);
		} else if (part instanceof AbstractDecoratedTextEditor) {
			AbstractDecoratedTextEditor editor= (AbstractDecoratedTextEditor)part;
			newFile= getEditorJavaFile(editor);
			sourceViewer= getEditorSourceViewer(editor);
		}
		if (newFile != null) {
			currentEditor= (EditorPart)part; //Should be EditorPart if newFile != null
			addEditor(currentEditor, newFile);
			if (!newFile.equals(currentFile)) {
				currentFile= newFile;
				if (Activator.isInDebugMode) {
					System.out.println("File:\"" + Logger.getPortableFilePath(currentFile) + "\"");
				}
			}
			if (listenedViewer != null) {
				listenedViewer.removeTextListener(trackerInstance);
			}
			listenedViewer= sourceViewer;
			if (listenedViewer != null) {
				listenedViewer.addTextListener(trackerInstance);
			}
		}
	}

	private void addEditor(EditorPart editor, IFile editedFile) {
		if (isConflictEditor(editor)) {
			CompareEditor compareEditor= (CompareEditor)editor;
			if (!openConflictEditors.contains(compareEditor)) {
				openConflictEditors.add(compareEditor);
				dirtyConflictEditors.add(compareEditor); //conflict editors are always dirty from the start
				logger.logOpenedConflictEditor(getConflictEditorID(compareEditor), getConflictEditorInitialContent(compareEditor), editedFile);
			}
		}
	}

	@Override
	public void textChanged(TextEvent event) {
		DocumentEvent documentEvent= event.getDocumentEvent();
		if (documentEvent != null && !isRefactoring) {
			if (isConflictEditor(currentEditor)) {
				CompareEditor compareEditor= (CompareEditor)currentEditor;
				dirtyConflictEditors.add(compareEditor);
				logger.logConflictEditorTextEvent(event, getConflictEditorID(compareEditor), isUndoing, isRedoing);
			} else {
				dirtyFiles.add(currentFile);
				logger.logTextEvent(event, currentFile, isUndoing, isRedoing);
			}
		}
	}

	@Override
	public void executionNotification(RefactoringExecutionEvent event) {
		int eventType= event.getEventType();
		if (eventType == RefactoringExecutionEvent.ABOUT_TO_PERFORM || eventType == RefactoringExecutionEvent.ABOUT_TO_REDO ||
				eventType == RefactoringExecutionEvent.ABOUT_TO_UNDO) {
			isRefactoring= true;
			logger.logRefactoringStarted();
		} else {
			isRefactoring= false;
			logger.logRefactoringExecutionEvent(event);
		}
	}

	@Override
	public void historyNotification(OperationHistoryEvent event) {
		int eventType= event.getEventType();
		if (eventType == OperationHistoryEvent.ABOUT_TO_UNDO) {
			isUndoing= true;
		} else {
			isUndoing= false;
		}
		if (eventType == OperationHistoryEvent.ABOUT_TO_REDO) {
			isRedoing= true;
		} else {
			isRedoing= false;
		}
		if (eventType == OperationHistoryEvent.UNDONE || eventType == OperationHistoryEvent.REDONE) {
			if (currentEditor != null && !isConflictEditor(currentEditor)) { //conflict editors remain dirty until saved
				if (currentEditor.isDirty()) {
					dirtyFiles.add(currentFile);
				} else {
					dirtyFiles.remove(currentFile);
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
			IWorkbenchPage activePage= activeWorkbenchWindow.getActivePage();
			if (activePage != null) {
				IEditorReference[] editorReferences= activePage.getEditorReferences();
				for (IEditorReference editorReference : editorReferences) {
					IEditorPart editor= editorReference.getEditor(false);
					if (editor != null && isConflictEditor(editor) && !editor.isDirty()) {
						CompareEditor compareEditor= (CompareEditor)editor;
						if (dirtyConflictEditors.contains(compareEditor)) {
							dirtyConflictEditors.remove(compareEditor);
							savedConflictEditorIDs.add(getConflictEditorID(compareEditor));
							changedJavaFiles.remove(getEditorJavaFile(compareEditor));
						}
					}
				}
			}
			boolean isSVNEntriesChanged= svnEntriesChangeSet.size() > 0;
			for (IFile file : changedJavaFiles) {
				if (!updatedJavaFiles.contains(file)) { //updated files are neither saved nor externally modified
					if (isRefactoring || dirtyFiles.contains(file) && !isSVNEntriesChanged) {
						savedJavaFiles.add(file);
					} else {
						externallyModifiedJavaFiles.add(file);
					}
				}
				//TODO: Removing from dirty files when updated or changed externally may cause subsequent save to be treated as an
				//external modification. Is it ok (e.g. this can be detected and filtered out during the replay phase)?
				dirtyFiles.remove(file);
			}
			dirtyFiles.removeAll(removedJavaFiles);
			logger.logSavedFiles(savedJavaFiles, isRefactoring);
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

	private boolean isJavaFile(IFile file) {
		String fileExtension= file.getFileExtension();
		if (fileExtension != null && fileExtension.equals("java")) { //$NON-NLS-1$
			return true;
		}
		return false;
	}

	@Override
	public void partActivated(IWorkbenchPart part) {
	}

	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
	}

	@Override
	public void partClosed(IWorkbenchPart part) {
		IFile closedFile= null;
		if (part instanceof CompareEditor) {
			closedFile= getEditorJavaFile((CompareEditor)part);
		} else if (part instanceof AbstractDecoratedTextEditor) {
			closedFile= getEditorJavaFile((AbstractDecoratedTextEditor)part);
		}
		if (closedFile != null) {
			if (isConflictEditor((EditorPart)part)) {
				CompareEditor compareEditor= (CompareEditor)part;
				openConflictEditors.remove(compareEditor);
				dirtyConflictEditors.remove(compareEditor);
				logger.logClosedConflictEditor(getConflictEditorID(compareEditor));
			} else {
				//Check that this is the last editor of this file that is closed
				IWorkbenchPage activePage= activeWorkbenchWindow.getActivePage();
				if (activePage != null) {
					IEditorReference[] editorReferences= activePage.getEditorReferences();
					for (IEditorReference editorReference : editorReferences) {
						IEditorPart editor= editorReference.getEditor(false);
						if (editor != part && !isConflictEditor(editor)) {
							IFile file= null;
							if (editor instanceof CompareEditor) {
								file= getEditorJavaFile((CompareEditor)editor);
							} else if (editor instanceof AbstractDecoratedTextEditor) {
								file= getEditorJavaFile((AbstractDecoratedTextEditor)editor);
							}
							if (closedFile.equals(file)) {
								return; // file is not really closed as it is opened in another editor
							}
						}
					}
				}
				dirtyFiles.remove(closedFile);
				logger.logClosedFile(closedFile);
			}
		}
	}

	@Override
	public void partDeactivated(IWorkbenchPart part) {
	}

	@Override
	public void partOpened(IWorkbenchPart part) {
	}

}
